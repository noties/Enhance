package io.noties.enhance;

import javax.annotation.Nullable;

public class ApiInfo {

    @Nullable
    public final Integer since;
    @Nullable public final Integer deprecated;

    public ApiInfo(@Nullable Integer since, @Nullable Integer deprecated) {
        this.since = since;
        this.deprecated = deprecated;
    }

    @Override
    public String toString() {
        return "ApiInfo{" +
                "since='" + since + '\'' +
                ", deprecated='" + deprecated + '\'' +
                '}';
    }
}
