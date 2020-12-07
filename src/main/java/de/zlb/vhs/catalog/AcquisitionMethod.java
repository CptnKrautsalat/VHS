package de.zlb.vhs.catalog;

public enum AcquisitionMethod {
    PURCHASE,
    GIFT,
    MANDATORY,
    OTHER;

    public static AcquisitionMethod fromString(String method) {
        return switch (method) {
            case "K" -> PURCHASE;
            case "G" -> GIFT;
            case "P" -> MANDATORY;
            default -> OTHER;
        };
    }
}
