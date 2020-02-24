package by.kes.queue.domain.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Error {

    private String message;
    private String description;
    private String code;
}
