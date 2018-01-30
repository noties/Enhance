package ru.noties.enhance;

import javax.annotation.Nonnull;

public abstract class ApiVersionFormatter {

    @Nonnull
    public static ApiVersionFormatter create() {
        return new Impl();
    }


    public abstract String format(@Nonnull ApiVersion apiVersion);


    private static class Impl extends ApiVersionFormatter {

        @Override
        public String format(@Nonnull ApiVersion apiVersion) {
            // @since 5.1 Lollipop (22)
            return apiVersion.getVersionName() + " " + apiVersion.getCodeName() + " (" + apiVersion.getSdkInt() + ")";
        }
    }
}
