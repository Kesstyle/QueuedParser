package by.kes.queue.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetMappingResponse extends Response {

    private List<Mapping> mappingList;

    public void addMapping(final Mapping mapping) {
        if (mappingList == null) {
            mappingList = new ArrayList<>();
        }
        mappingList.add(mapping);
    }
}
