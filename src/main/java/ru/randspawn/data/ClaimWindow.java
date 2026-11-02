package ru.randspawn.data;

public record ClaimWindow(long deadlineEpochMillis, boolean consumed) {
    public boolean isAvailableAt(long nowEpochMillis) {
        return !consumed && nowEpochMillis <= deadlineEpochMillis;
    }

    public ClaimWindow consume() {
        return new ClaimWindow(deadlineEpochMillis, true);
    }
}
