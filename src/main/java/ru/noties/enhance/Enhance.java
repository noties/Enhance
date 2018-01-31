package ru.noties.enhance;

import org.apache.commons.io.FileUtils;
import ru.noties.enhance.options.EnhanceOptions;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

import static ru.noties.enhance.Log.log;

public class Enhance {

    private static final String APP_FOLDER = ".enhance-backup";

    public static void main(String[] args) {

        log("[Enhance] version: %s", EnhanceVersion.NAME);

        final long start = System.currentTimeMillis();

        final EnhanceOptions options = EnhanceOptions.create(args);

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

        final File sdkSources = sdkHelper.source();

        final File source;
        {
            final String folder = sdkHelper.folder();
            final File file = new File(appFolder, folder);
            if (file.exists()) {
                source = file;
            } else {

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

                source = file;
            }
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

        final EnhanceWriter writer = EnhanceWriter.create(options.sourceFormat(), store, ApiVersionFormatter.create());
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
