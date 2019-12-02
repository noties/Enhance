package ru.noties.enhance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class ApiInfoStore {

    @Nonnull
    public static ApiInfoStore create(@Nonnull File apiVersions) {
        return new ApiInfoStoreImpl(apiVersions);
    }

    static class TypeVersion extends ApiInfo {

        final Map<String, ApiInfo> fields = new HashMap<>(3);
        final Map<String, ApiInfo> methods = new HashMap<>(3);

        TypeVersion(ApiVersion since, ApiVersion deprecated) {
            super(since, deprecated);
        }
    }

    @Nullable
    public abstract ApiInfo type(@Nonnull String type);

    @Nullable
    public abstract ApiInfo field(@Nonnull String type, @Nonnull String name);

    @Nullable
    public abstract ApiInfo method(@Nonnull String type, @Nonnull String signature);

    @Nonnull
    public abstract Map<String, TypeVersion> info();
}
