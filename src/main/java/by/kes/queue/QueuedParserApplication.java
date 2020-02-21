package by.kes.queue;

import by.kes.queue.domain.SiteItems;
import by.kes.queue.domain.SiteItemsResponse;
import by.kes.queue.domain.SitePattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
@EnableDiscoveryClient
public class QueuedParserApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueuedParserApplication.class, args);
	}

	final RestTemplate restTemplate = new RestTemplate();
	final Map<String, SitePattern> siteMappings;

	{
		siteMappings = new HashMap<>();
		siteMappings.put("e-bread", new SitePattern("https://e-dostavka.by/catalog/400000170.html", "class=title,c0-c0"));
		siteMappings.put("e-chef", new SitePattern("https://e-dostavka.by/catalog/4687.html", "class=title,c0-c0"));
		siteMappings.put("e-salads", new SitePattern("https://e-dostavka.by/catalog/4690.html", "class=title,c0-c0"));
		siteMappings.put("e-sushi", new SitePattern("https://e-dostavka.by/catalog/4685.html", "class=title,c0-c0"));
	}

	@Autowired
	private DiscoveryClient discoveryClient;

	@GetMapping(path = "/api/v1/ping")
	public String ping(@RequestParam(defaultValue = "Kes") String name) {
		return String.format("Hello %s from Queued parser. He's aware of %s", name,
				discoveryClient.getInstances("kes-html-consuled"));
	}

	@GetMapping(path = "/api/v1/parse")
	public SiteItemsResponse getSiteItems(@RequestParam List<String> sites) {
		final ServiceInstance instance = discoveryClient.getInstances("kes-html-consuled").get(0);
		final String url = "http://localhost:" + instance.getPort() + "/api/v1/elements";

		final List<SiteItems> siteItemsList = sites.stream().map(s -> getSiteItems(url,
				siteMappings.get(s).getPatternToParse(), restTemplate, siteMappings.get(s).getUrl())).collect(Collectors.toList());

		final SiteItemsResponse result = new SiteItemsResponse();
		result.setSiteItems(siteItemsList);
		return result;
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
