package io.noties.enhance;

public class ApiInfo {

    public final ApiVersion since;
    public final ApiVersion deprecated;

    public ApiInfo(ApiVersion since, ApiVersion deprecated) {
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
