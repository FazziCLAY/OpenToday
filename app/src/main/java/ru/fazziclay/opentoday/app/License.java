package ru.fazziclay.opentoday.app;

/**
 * OpenSource license object
 */
public class License {
    private final String assetPath;
    private final String title;
    private final String description;

    public License(String assetPath, String title, String description) {
        this.assetPath = assetPath;
        this.title = title;
        this.description = description;
    }

    public String getAssetPath() {
        return assetPath;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
