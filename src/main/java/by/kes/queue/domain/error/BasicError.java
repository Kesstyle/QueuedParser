package by.kes.queue.domain.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public enum BasicError {

    INVALID_REQUEST("Invalid request", "The data you've sent in request is not applicable for this call", "1"),
    UNKNOWN_ERROR("Unknown service error", "Unprocessible error occurred, please try again later", "2"),
    NO_MAPPING_ERROR("Unknown service error", "Unprocessible error occurred, please try again later", "3");

    private String message;
    private String description;
    private String code;
}
