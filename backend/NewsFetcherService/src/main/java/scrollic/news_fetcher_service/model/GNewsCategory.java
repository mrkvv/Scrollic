package scrollic.news_fetcher_service.model;

import java.util.Arrays;
import java.util.List;

public enum GNewsCategory {
    GENERAL("general"),
    WORLD("world"),
    TECHNOLOGY("technology"),
    SPORTS("sports"),
    SCIENCE("science"),
    BUSINESS("business"),
    HEALTH("health"),
    ENTERTAINMENT("entertainment");

    private final String value;

    GNewsCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static List<GNewsCategory> getAllCategories() {
        return Arrays.asList(GNewsCategory.values());
    }
}
