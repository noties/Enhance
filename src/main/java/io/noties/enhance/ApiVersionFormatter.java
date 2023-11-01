package io.noties.enhance;

import javax.annotation.Nonnull;

public abstract class ApiVersionFormatter {

    @Nonnull
    public static ApiVersionFormatter create() {
        return new Impl();
    }

    @Nonnull
    public abstract String format(int version);


    private static class Impl extends ApiVersionFormatter {

        @Nonnull
        @Override
        public String format(int version) {
            final Api api = Api.of(version);
            if (api != null) {
                // for example - @since 5.1 Lollipop (22)
                return api.versionName + " " + api.codeName + " (" + api.sdkInt + ")";
            }
            return "unknown (" + version + ")";
        }
    }
}
