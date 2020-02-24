package by.kes.queue.domain.error;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class CustomError extends Error {

    public CustomError(final String message, final String description, final String code) {
        super(message, description, code);
    }
}
