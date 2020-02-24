package by.kes.queue.domain.exception;

import lombok.Data;

@Data
public abstract class GeneralException extends RuntimeException {

    private String code;
    private String description;

    public GeneralException(final String message, final String description, final String code,
                                   final Throwable throwable) {
        super(message, throwable);
        this.code = code;
        this.description = description;
    }
}
