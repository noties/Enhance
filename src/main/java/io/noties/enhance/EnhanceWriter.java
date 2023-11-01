package io.noties.enhance;

import io.noties.enhance.options.SourceFormat;

import javax.annotation.Nonnull;
import java.io.File;

public abstract class EnhanceWriter {

    @Nonnull
    public static EnhanceWriter create(
            int sdk,
            @Nonnull SourceFormat format,
            @Nonnull ApiInfoStore apiInfoStore,
            @Nonnull ApiVersionFormatter apiVersionFormatter
    ) {
        return new EnhanceWriterImpl(sdk, format, apiInfoStore, apiVersionFormatter);
    }

    public abstract void write(@Nonnull File source, @Nonnull File destination);
}
