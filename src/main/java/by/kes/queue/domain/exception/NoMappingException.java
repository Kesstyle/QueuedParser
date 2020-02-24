package by.kes.queue.domain.exception;

import lombok.Data;

@Data
public class NoMappingException extends GeneralException {

    public NoMappingException(final String message, final String description, final String code,
                                   final Throwable throwable) {
        super(message, description, code, throwable);
    }

    public NoMappingException(final String message, final String description, final String code) {
        super(message, description, code, null);
    }
}
