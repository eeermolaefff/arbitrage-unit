package com.stambul.library.tools;

public class PreparationTools {

    public static String addressPreparation(String address) {
        if (address == null)
            throw new IllegalArgumentException("Null address received");
        return address.toLowerCase().replace(" ", "");
    }

    public static String currencyCategoryPreparation(String category) {
        return category != null ? category.toLowerCase() : null;
    }

    public static String slugPreparation(String slug) {
        if (slug == null)
            throw new IllegalArgumentException("Null slug received");
        return slug.toLowerCase();
    }

    public static String exchangeTypePreparation(String exchangeType) {
        if (exchangeType == null)
            throw new IllegalArgumentException("Null exchange type received");
        return exchangeType.toLowerCase();
    }

    public static String exchangeCategoryPreparation(String exchangeCategory) {
        if (exchangeCategory == null)
            throw new IllegalArgumentException("Null exchange category received");
        return exchangeCategory.toLowerCase();
    }
}
