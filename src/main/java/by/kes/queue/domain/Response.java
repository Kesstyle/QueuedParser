package by.kes.queue.domain;

import by.kes.queue.domain.error.Error;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static by.kes.queue.domain.ResponseStatus.*;

@Data
public class Response {

    private List<Error> errorList;
    private List<Warning> warningList;
    private ResponseStatus status = SUCCESS;

    public void addError(final Error error) {
        if (errorList == null) {
            errorList = new ArrayList<>();
        }
        errorList.add(error);
        if (eligibleForStatusDecrease(status, ERROR)) {
            status = ERROR;
        }
    }

    public void addWarning(final Warning warning) {
        if (warningList == null) {
            warningList = new ArrayList<>();
        }
        warningList.add(warning);
        if (eligibleForStatusDecrease(status, WARNING)) {
            status = WARNING;
        }
    }
}
