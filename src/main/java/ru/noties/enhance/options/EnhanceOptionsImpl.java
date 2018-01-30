package ru.noties.enhance.options;

import org.apache.commons.cli.*;

import javax.annotation.Nonnull;

class EnhanceOptionsImpl extends EnhanceOptions {

    private static final String SDK_PATH = "sp";
    private static final String FORMAT = "format";
    private static final String SDK = "sdk";
    private static final String HELP = "h";

    private final CommandLine commandLine;

    EnhanceOptionsImpl(String[] args) {

        final Options options = createOptions();
        final CommandLineParser parser = new DefaultParser();

        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            // print help and exit
            new HelpFormatter().printHelp("Enhance", options);
            throw new IllegalStateException(e);
        }

        if (commandLine.hasOption('h')) {
            new HelpFormatter().printHelp("Enhance", options);
        }
    }

    @Nonnull
    @Override
    public String androidSdkPath() {
        final String out;
        if (commandLine.hasOption(SDK_PATH)) {
            out = commandLine.getOptionValue(SDK_PATH);
        } else {
            final String system = System.getenv("ANDROID_HOME");
            if (system == null
                    || system.length() == 0) {
                throw new IllegalStateException("Cannot find 'ANDROID_HOME' system variable. Define it on " +
                        "the system level or specify with `-p` option");
            }
            out = system;
        }
        return out;
    }

    @Nonnull
    @Override
    public SourceFormat sourceFormat() {

        final SourceFormat format;

        final String value = commandLine.getOptionValue(FORMAT, "");

        if ("aosp".equals(value)) {
            format = SourceFormat.AOSP;
        } else if ("google".equals(value)) {
            format = SourceFormat.GOOGLE;
        } else {
            format = SourceFormat.NONE;
        }

        return format;
    }

    @Override
    public int sdk() {
        final String value = commandLine.getOptionValue(SDK, "0");
        return Integer.parseInt(value);
    }

    @Nonnull
    private static Options createOptions() {

        final Options options = new Options();

        options.addOption(SDK_PATH, "sdk-path", true, "Path to Android SDK. If not " +
                "specified 'ANDROID_HOME' system variable will be used");

        options.addOption(FORMAT, true, "Format sources. Accepts (aosp|google). Everything else " +
                "would keep original formatting");

        options.addOption(Option.builder(SDK)
                .required(true)
                .hasArg(true)
                .desc("Specify which SDK version to process.")
                .build());

        options.addOption(HELP, "help", false, "Prints help");

        return options;
    }
}
