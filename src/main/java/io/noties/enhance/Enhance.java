package io.noties.enhance;

import io.noties.enhance.options.EnhanceOptions;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import static io.noties.enhance.Log.log;
import static io.noties.enhance.Stats.printStatsFor;

public class Enhance {

    private static final String APP_FOLDER = ".enhance-backup";

    public static void main(String[] args) {

        final ApiVersionFormatter apiVersionFormatter = ApiVersionFormatter.create();

        log("[Enhance] version: %s", EnhanceVersion.NAME);
        log("[Enhance] latest Android SDK version: %s", apiVersionFormatter.format(ApiVersion.latest()));
        log("[Enhance] https://github.com/noties/Enhance");

        final EnhanceOptions options = EnhanceOptions.create(args);

        // @since 1.0.2
        // check if we have this version info included and ask user if he/she want to proceed if
        // supplied sdk is not known to this library version
        final ApiVersion apiVersion = ApiVersion.of(options.sdk());
        if (apiVersion.isUnknown()) {

            System.err.printf(Locale.US, "[Enhance] WARNING: specified SDK version %d (`%s`) is unknown to this " +
                            "library version, do you wish to proceed anyway? (Y|N)%n", options.sdk(),
                    apiVersionFormatter.format(apiVersion));

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                final String line = reader.readLine();
                if (!"y".equalsIgnoreCase(line)) {
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        final long start = System.currentTimeMillis();

        log("[Enhance] obtaining required files/folders");

        final SdkHelper sdkHelper = SdkHelper.create(options);

        log("[Enhance] obtaining application backup directory");

        final File appFolder = new File(System.getProperty("user.home"), APP_FOLDER);
        if (!appFolder.exists()) {
            if (!appFolder.mkdirs()) {
                throw new RuntimeException("Cannot create application backup directory at path: " + appFolder.getPath());
            }
        }

        log("[Enhance] parsing api-versions.xml");

        final ApiInfoStore store = ApiInfoStore.create(sdkHelper.apiVersions());
        if (options.emitDiff()) {
            log("[Enhance] emit diff for %d SDK level (%s %s)",
                    apiVersion.getSdkInt(),
                    apiVersion.getCodeName(),
                    apiVersion.getVersionName());
            printStatsFor(apiVersion, store.info());
            return;
        }

        final File sdkSources = sdkHelper.source();

        final File source;
        {
            final String folder = sdkHelper.folder();
            final File file = new File(appFolder, folder);
            if (!file.exists()) {

                if (!file.mkdirs()) {
                    throw new RuntimeException("Cannot create android sources backup folder at: " + file.getPath());
                }

                // backup sources first

                log("[Enhance] backing up android sources, from: `%s` to: `%s`", sdkSources.getPath(), file.getPath());

                try {
                    FileUtils.copyDirectory(sdkSources, file);
                } catch (IOException e) {

                    // let's try to remove backup directory
                    try {
                        FileUtils.cleanDirectory(file);
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    } catch (IOException e1) {
                        // no op
                    }

                    throw new RuntimeException(e);
                }

            }
            source = file;
        }

        // now, we duplicate files from backup to source, if it's java and there api info -> parse and api info

        final File[] files = source.listFiles();
        if (files == null
                || files.length == 0) {
            throw new RuntimeException("Unexpected state of the source directory: it is empty. Try removing it first: " + source.getPath());
        }

        log("[Enhance] cleaning the original source folder: `%s`", sdkSources.getPath());

        try {
            FileUtils.cleanDirectory(sdkSources);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log("[Enhance] processing source files");

        final EnhanceWriter writer = EnhanceWriter.create(
                apiVersion,
                options.sourceFormat(),
                store,
                apiVersionFormatter
        );
        writer.write(source, sdkSources);

        final long took = System.currentTimeMillis() - start;

        log("[Enhance] processing took: %s", format(took));
    }

    @Nonnull
    private static String format(long took) {

        final long second = 1000L;
        final long minute = second * 60;

        final long minutes = took / minute;
        took -= (minutes * minute);
        final long seconds = took / second;

        return String.format("%02d minutes %02d seconds", minutes, seconds);
    }
}
