package ru.noties.enhance;

import ru.noties.enhance.options.SourceFormat;

import javax.annotation.Nonnull;
import java.io.File;

public abstract class EnhanceWriter {

    @Nonnull
    public static EnhanceWriter create(
            @Nonnull SourceFormat format,
            @Nonnull ApiInfoStore apiInfoStore,
            @Nonnull ApiVersionFormatter apiVersionFormatter
    ) {
        return new EnhanceWriterImpl(format, apiInfoStore, apiVersionFormatter);
    }

    public abstract void write(@Nonnull File source, @Nonnull File destination);
}
