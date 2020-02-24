package by.kes.queue.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Mapping {

    private String alias;
    private String url;
    private String pattern;
}
