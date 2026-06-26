package com.weinsim.slpaint.main.image;

public enum ImageFormat {

    PNG("png", true),
    JPG("JPEG Files", new String[] { "jpg", "jpeg" }, false),
    BMP("bmp", false);
    // WBMP("wbmp"),
    // GIF("gif");

    public final String name;
    public final String[] extensions;
    public final boolean supportsAlpha;

    private ImageFormat(String extension, boolean supportsAlpha) {
        this("%s Files".formatted(extension.toUpperCase()), new String[] { extension }, supportsAlpha);
    }

    private ImageFormat(String name, String[] extensions, boolean supportsAlpha) {
        this.name = name;
        this.extensions = extensions;
        this.supportsAlpha = supportsAlpha;
    }

    public static ImageFormat fromString(String formatName) {
        formatName = formatName.toLowerCase();
        for (ImageFormat format : values()) {
            for (String extension : format.extensions) {
                if (formatName.equals(extension)) {
                    return format;
                }
            }
        }
        return null;
    }

}
