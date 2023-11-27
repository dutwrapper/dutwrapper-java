package io.dutwrapper.dutwrapper.model.enums;

public enum NewsType {
    Global("CTRTBSV"),
    Subject("CTRTBGV");

    private String value;
    private NewsType(String s) {
        this.value = s;
    }

    public String toString() {
        return value;
    }
}