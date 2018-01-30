package ru.noties.enhance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public abstract class ApiInfoStore {

    @Nonnull
    public static ApiInfoStore create(@Nonnull File apiVersions) {
        return new ApiInfoStoreImpl(apiVersions);
    }

    @Nullable
    public abstract ApiInfo type(@Nonnull String type);

    @Nullable
    public abstract ApiInfo field(@Nonnull String type, @Nonnull String name);

    @Nullable
    public abstract ApiInfo method(@Nonnull String type, @Nonnull String signature);
}
