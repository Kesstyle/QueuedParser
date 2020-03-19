package by.kes.queue.proxy;

import by.kes.queue.configuration.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "${html.parser.alias}", configuration = FeignConfig.class)
public interface HtmlParserClient {

    @GetMapping("${api.html.elements}")
    String[] parsePage(@RequestHeader("Target-URL") String siteUrl,
                           @RequestHeader("Target-html-template") String pattern);

    @GetMapping("${api.html.ping}")
    String ping(@RequestParam(defaultValue = "Kes") String name);

}
