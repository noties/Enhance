package ru.noties.enhance;

import ru.noties.enhance.options.EnhanceOptions;

import javax.annotation.Nonnull;
import java.io.File;

public abstract class SdkHelper {

    @Nonnull
    public static SdkHelper create(@Nonnull EnhanceOptions options) {
        return new Impl(options);
    }

    @Nonnull
    public abstract String folder();

    @Nonnull
    public abstract File apiVersions();

    @Nonnull
    public abstract File source();


    private static class Impl extends SdkHelper {

        private final String folder;

        private final File apiVersions;
        private final File source;

        private Impl(@Nonnull EnhanceOptions options) {

            final File platforms = new File(options.androidSdkPath(), "platforms");
            final File sources = new File(options.androidSdkPath(), "sources");

            if (!platforms.exists()) {
                throw new IllegalStateException("Cannot find 'platforms' folder at specified path: " + platforms.getPath());
            }

            if (!sources.exists()) {
                throw new IllegalStateException("Cannot find 'sources' folder at specified path: " + sources.getPath());
            }

            folder = "android-" + options.sdk();

            apiVersions = new File(platforms, folder + "/data/api-versions.xml");

            if (!apiVersions.exists()) {
                throw new IllegalStateException("Cannot find 'api-versions.xml' file at the specified path: " + apiVersions.getPath());
            }

            source = new File(sources, folder);

            if (!source.exists()) {
                throw new IllegalStateException("Cannot find '" + folder + "' folder at specified path: " + source.getPath());
            }
        }

        @Nonnull
        @Override
        public String folder() {
            return folder;
        }

        @Nonnull
        @Override
        public File apiVersions() {
            return apiVersions;
        }

        @Nonnull
        @Override
        public File source() {
            return source;
        }
    }
}
