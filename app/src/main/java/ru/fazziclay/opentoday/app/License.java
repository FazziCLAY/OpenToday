package ru.fazziclay.opentoday.app;

/**
 * OpenSource license object
 */
public class License {
    private final String assetPath;
    private final String title;
    private final String url;

    public License(String assetPath, String title, String url) {
        this.assetPath = assetPath;
        this.title = title;
        this.url = url;
    }

    public String getAssetPath() {
        return assetPath;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
