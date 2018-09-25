package ru.noties.enhance;

import javax.annotation.Nonnull;

public class ApiVersion {

    @Nonnull
    public static ApiVersion latest() {
        return VERSIONS[VERSIONS.length - 1];
    }

    @Nonnull
    public static ApiVersion of(int sdkInt) {
        final ApiVersion version;
        if (sdkInt < 0
                || (sdkInt - 1) >= LENGTH) {
            version = new ApiVersion(sdkInt, "unknown", "unknown", true);
        } else {
            version = VERSIONS[sdkInt - 1];
        }
        return version;
    }

    private final int sdkInt;
    private final String versionName;
    private final String codeName;
    private final boolean unknown;

    private ApiVersion(int sdkInt, @Nonnull String versionName, @Nonnull String codeName) {
        this(sdkInt, versionName, codeName, false);
    }

    private ApiVersion(int sdkInt, @Nonnull String versionName, @Nonnull String codeName, boolean unknown) {
        this.sdkInt = sdkInt;
        this.versionName = versionName;
        this.codeName = codeName;
        this.unknown = unknown;
    }

    public int getSdkInt() {
        return sdkInt;
    }

    @Nonnull
    public String getVersionName() {
        return versionName;
    }

    @Nonnull
    public String getCodeName() {
        return codeName;
    }

    public boolean isUnknown() {
        return unknown;
    }

    @Override
    public String toString() {
        return "ApiVersion{" +
                "sdkInt=" + sdkInt +
                ", versionName='" + versionName + '\'' +
                ", codeName='" + codeName + '\'' +
                ", unknown=" + unknown +
                '}';
    }

    private static final ApiVersion[] VERSIONS = {
            new ApiVersion(1, "1.0", "(initial)"),
            new ApiVersion(2, "1.1", "(initial)"),
            new ApiVersion(3, "1.5", "Cupcake"),
            new ApiVersion(4, "1.6", "Donut"),
            new ApiVersion(5, "2.0", "Eclair"),
            new ApiVersion(6, "2.0.1", "Eclair"),
            new ApiVersion(7, "2.1", "Eclair"),
            new ApiVersion(8, "2.2", "Froyo"),
            new ApiVersion(9, "2.3", "Gingerbread"),
            new ApiVersion(10, "2.3.3", "Gingerbread"),
            new ApiVersion(11, "3.0", "Honeycomb"),
            new ApiVersion(12, "3.1", "Honeycomb"),
            new ApiVersion(13, "3.2", "Honeycomb"),
            new ApiVersion(14, "4.0", "Ice Scream Sandwich"),
            new ApiVersion(15, "4.0.3", "Ice Scream Sandwich"),
            new ApiVersion(16, "4.1", "Jelly Bean"),
            new ApiVersion(17, "4.2", "Jelly Bean"),
            new ApiVersion(18, "4.3", "Jelly Bean"),
            new ApiVersion(19, "4.4", "Kitkat"),
            new ApiVersion(20, "4.4W", "Kitkat"),
            new ApiVersion(21, "5.0", "Lollipop"),
            new ApiVersion(22, "5.1", "Lollipop"),
            new ApiVersion(23, "6.0", "Marshmallow"),
            new ApiVersion(24, "7.0", "Nougat"),
            new ApiVersion(25, "7.1", "Nougat"),
            new ApiVersion(26, "8.0", "Oreo"),
            new ApiVersion(27, "8.1", "Oreo"),
            new ApiVersion(28, "9.0", "Pie")
    };

    private static final int LENGTH = VERSIONS.length;
}
