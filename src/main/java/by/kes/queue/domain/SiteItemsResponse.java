package by.kes.queue.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SiteItemsResponse extends Response {

    private List<SiteItems> siteItems;
}
