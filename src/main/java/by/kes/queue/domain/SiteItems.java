package by.kes.queue.domain;

import lombok.Data;

import java.util.List;

@Data
public class SiteItems {

    private String siteUrl;
    private List<String> items;
}
