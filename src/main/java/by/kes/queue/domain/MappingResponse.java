package by.kes.queue.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MappingResponse extends Response {

    private List<String> processedAlliases;

    public void addProcessedMapping(final String processedMapping) {
        if (processedAlliases == null) {
            processedAlliases = new ArrayList<>();
        }
        processedAlliases.add(processedMapping);
    }
}
