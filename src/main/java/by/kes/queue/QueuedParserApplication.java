package by.kes.queue;

import by.kes.queue.domain.error.BasicError;
import by.kes.queue.domain.Mapping;
import by.kes.queue.domain.*;
import by.kes.queue.domain.error.CustomError;
import by.kes.queue.domain.exception.InvalidRequestException;
import by.kes.queue.domain.exception.NoMappingException;
import by.kes.queue.domain.exception.UnknownErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static by.kes.queue.domain.error.BasicError.INVALID_REQUEST;
import static by.kes.queue.domain.error.BasicError.UNKNOWN_ERROR;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.util.CollectionUtils.isEmpty;

@SpringBootApplication
@RestController
@EnableDiscoveryClient
@Slf4j
public class QueuedParserApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueuedParserApplication.class, args);
	}

	final RestTemplate restTemplate = new RestTemplate();
	final Map<String, SitePattern> siteMappings;

	{
		siteMappings = new ConcurrentHashMap<>();
		siteMappings.put("e-bread", new SitePattern("https://e-dostavka.by/catalog/400000170.html", "class=title,c0-c0"));
		siteMappings.put("e-chef", new SitePattern("https://e-dostavka.by/catalog/4687.html", "class=title,c0-c0"));
		siteMappings.put("e-salads", new SitePattern("https://e-dostavka.by/catalog/4690.html", "class=title,c0-c0"));
		siteMappings.put("e-sushi", new SitePattern("https://e-dostavka.by/catalog/4685.html", "class=title,c0-c0"));
	}

	@Value("${api.html.elements}")
	private String htmlElementsEndpoint;

	@Value("${api.html.ping}")
	private String htmlPingEndpoint;

	@Autowired
	private DiscoveryClient discoveryClient;

	@GetMapping(path = "/api/v1/ping")
	public String ping(@RequestParam(defaultValue = "Kes") String name) {
		return String.format("Hello %s from Queued parser. He's aware of %s", name,
				discoveryClient.getInstances("kes-html-consuled"));
	}

	@GetMapping(path = "/api/v1/mapping")
	public GetMappingResponse getMappings() {
		final GetMappingResponse response = new GetMappingResponse();
		siteMappings.entrySet().stream()
				.forEach(e -> response.addMapping(Mapping.builder()
						.alias(e.getKey())
						.pattern(e.getValue().getPatternToParse())
						.url(e.getValue().getUrl()).build()));
		return response;
	}

	@DeleteMapping(path = "/api/v1/mapping")
	public MappingResponse deleteMapping(@RequestParam final List<String> aliases) {
		if (CollectionUtils.isEmpty(aliases)) {
			throw new InvalidRequestException("Request is empty", "Request is empty or with empty data", "API-E1");
		}
		final MappingResponse response = new MappingResponse();
		aliases.stream().forEach(a -> {
			siteMappings.remove(a);
			response.addProcessedMapping(a);
		});
		return response;
	}

	@GetMapping(path = "/api/v1/mapping/{alias}")
	public GetMappingResponse getMapping(@PathVariable final String alias) {
		if (StringUtils.isEmpty(alias)) {
			throw new InvalidRequestException("Empty request", "Alias should not be empty", "API-E3");
		}
		final SitePattern sitePattern = siteMappings.get(alias);
		final GetMappingResponse response = new GetMappingResponse();
		if (sitePattern != null) {
			response.addMapping(Mapping.builder().alias(alias)
					.url(sitePattern.getUrl())
					.pattern(sitePattern.getPatternToParse()).build());
		}
		return response;
	}

	@PostMapping(path = "/api/v1/mapping")
	public MappingResponse addMapping(@RequestBody final MappingRequest request) {
		if (request == null || isEmpty(request.getMappings())) {
			throw new InvalidRequestException("Request is empty", "Request is empty or with empty data", "API-E1");
		}
		for (int i = 0; i < request.getMappings().size(); i++) {
			final Mapping mapping = request.getMappings().get(i);
			if (StringUtils.isEmpty(mapping.getAlias()) || StringUtils.isEmpty(mapping.getUrl())
				|| StringUtils.isEmpty(mapping.getPattern())) {
				throw new InvalidRequestException("Request contains empty mappings",
						"Request contains empty mappings, all alias, patterns and urls must not be empty", "API-E2");
			}
		}
		final MappingResponse response = new MappingResponse();
		request.getMappings().stream().forEach(m ->  {
			siteMappings.put(m.getAlias(),
					new SitePattern(m.getUrl(), m.getPattern()));
			response.addProcessedMapping(m.getAlias());
		});

		return response;
	}

	@GetMapping(path = "/api/v1/parse")
	public SiteItemsResponse getSiteItems(@RequestParam List<String> sites) {
		if (CollectionUtils.isEmpty(sites)) {
			throw new InvalidRequestException("Request is empty", "Request is empty or with empty data", "API-E1");
		}
		for (int i = 0; i < sites.size(); i++) {
			if (siteMappings.get(sites.get(i)) == null) {
				throw new NoMappingException("No mapping", "We don't have mapping for alias provided: " + sites.get(i),
						"API-E4");
			}
		}
		final ServiceInstance instance = discoveryClient.getInstances("kes-html-consuled").get(0);
		final String url = instance.getUri() + htmlElementsEndpoint;

		final List<SiteItems> siteItemsList = sites.stream().map(s -> getSiteItems(url,
				siteMappings.get(s).getPatternToParse(), restTemplate, siteMappings.get(s).getUrl())).collect(Collectors.toList());

		final SiteItemsResponse result = new SiteItemsResponse();
		result.setSiteItems(siteItemsList);
		return result;
	}


	@ExceptionHandler(InvalidRequestException.class)
	@ResponseStatus(BAD_REQUEST)
	Response handleBadRequest(final HttpServletRequest request, final HttpServletResponse response,
							  final Exception ex) {
		return processError(request, response, ex, INVALID_REQUEST);
	}

	@ExceptionHandler(UnknownErrorException.class)
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	Response handleUnknownError(final HttpServletRequest request, final HttpServletResponse response,
							  final Exception ex) {
		return processError(request, response, ex, UNKNOWN_ERROR);
	}

	@ExceptionHandler(NoMappingException.class)
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	Response handleNoMappingError(final HttpServletRequest request, final HttpServletResponse response,
								final NoMappingException ex) {
		log.error("Exception occured: " + ex);
		final Response errorResponse = new Response();
		errorResponse.addError(new CustomError(ex.getMessage(), ex.getDescription(), ex.getCode()));
		log.error("Error response: {}", errorResponse);
		return errorResponse;
	}

	private Response processError(final HttpServletRequest request, final HttpServletResponse response,
								  final Exception ex, final BasicError basicError) {
		log.error("Exception occured: " + ex);
		final Response errorResponse = new Response();
		errorResponse.addError(new CustomError(basicError.getMessage(),
				basicError.getDescription(), basicError.getCode()));
		log.error("Error response: {}", errorResponse);
		return errorResponse;
	}

	private SiteItems getSiteItems(final String url, final String pattern, final RestTemplate restTemplate,
								   final String siteUrl) {
		final HttpHeaders headers = new HttpHeaders();
		headers.add("Target-URL", siteUrl);
		headers.add("Target-html-template", pattern);
		final RequestEntity requestEntity = new RequestEntity(headers, HttpMethod.GET,
				UriComponentsBuilder.fromHttpUrl(url).build().toUri());
		final List<String> strs = (List<String>) restTemplate.exchange(requestEntity, List.class)
				.getBody().stream().map(Objects::toString).collect(Collectors.toList());
		final SiteItems siteItem = new SiteItems();
		siteItem.setSiteUrl(siteUrl);
		siteItem.setItems(strs);
		return siteItem;
	}

}
