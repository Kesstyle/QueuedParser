package by.kes.queue.domain.exception;

import lombok.Data;

@Data
public class UnknownErrorException extends GeneralException {

    public UnknownErrorException(final String message, final String description, final String code,
                                   final Throwable throwable) {
        super(message, description, code, throwable);
    }
}
