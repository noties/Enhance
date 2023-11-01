package io.noties.enhance.options;

import javax.annotation.Nonnull;

public abstract class EnhanceOptions {

    @Nonnull
    public static EnhanceOptions create(String[] args) {
        return new EnhanceOptionsImpl(args);
    }

    @Nonnull
    public abstract String androidSdkPath();

    @Nonnull
    public abstract SourceFormat sourceFormat();

    public abstract boolean emitDiff();

    public abstract int sdk();
}
