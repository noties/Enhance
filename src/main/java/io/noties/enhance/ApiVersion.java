package io.noties.enhance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ApiVersion {

    @Nonnull
    public static ApiVersion latest() {
        return new ApiVersion(Api.latest());
    }

    @Nonnull
    public static ApiVersion of(int sdkInt) {
        final Api api = Api.of(sdkInt);
        final ApiVersion apiVersion;
        if (api == null) {
            apiVersion = new ApiVersion(sdkInt, "unknown", "unknown");
        } else {
            apiVersion = new ApiVersion(api);
        }
        return apiVersion;
    }

    @Nullable
    private final Api api;
    private final int sdkInt;
    private final String versionName;
    private final String codeName;

    private ApiVersion(@Nonnull Api api) {
        this.api = api;
        this.sdkInt = api.sdkInt;
        this.versionName = api.versionName;
        this.codeName = api.codeName;
    }

    private ApiVersion(int sdkInt, @Nonnull String versionName, @Nonnull String codeName) {
        this.api = null;
        this.sdkInt = sdkInt;
        this.versionName = versionName;
        this.codeName = codeName;
    }

    @Nullable
    public Api api() {
        return api;
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
        return api == null;
    }

    @Override
    public String toString() {
        if (api == null) {
            return "ApiVersion{" +
                    "sdkInt=" + sdkInt +
                    ", versionName='" + versionName + '\'' +
                    ", codeName='" + codeName + '\'' +
                    '}';
        } else {
            return "ApiVersion{" +
                    "api=" + api +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiVersion that = (ApiVersion) o;
        return sdkInt == that.sdkInt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sdkInt);
    }
}
