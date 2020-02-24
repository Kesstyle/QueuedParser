package by.kes.queue.domain;

public enum ResponseStatus {

    SUCCESS, WARNING, ERROR;

    public static boolean eligibleForStatusDecrease(final ResponseStatus initial, final ResponseStatus target) {
        return initial.ordinal() < target.ordinal();
    }
}
