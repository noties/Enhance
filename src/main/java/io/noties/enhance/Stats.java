package io.noties.enhance;

import javax.annotation.Nonnull;
import java.util.*;

abstract class Stats {

    static void printStatsFor(@Nonnull ApiVersion version, @Nonnull Map<String, ApiInfoStore.TypeVersion> info) {

        // filter
        final Map<String, ApiInfoStore.TypeVersion> filtered = new HashMap<>();

        for (Map.Entry<String, ApiInfoStore.TypeVersion> types : info.entrySet()) {

            final ApiInfoStore.TypeVersion original = types.getValue();
            final ApiInfoStore.TypeVersion typeVersion = new ApiInfoStore.TypeVersion(original.since, original.deprecated);

            for (Map.Entry<String, ApiInfo> fields : original.fields.entrySet()) {
                if (shouldEmit(version, fields.getValue())) {
                    typeVersion.fields.put(fields.getKey(), fields.getValue());
                }
            }

            for (Map.Entry<String, ApiInfo> methods : original.methods.entrySet()) {
                if (shouldEmit(version, methods.getValue())) {
                    typeVersion.methods.put(methods.getKey(), methods.getValue());
                }
            }

            if (shouldEmit(version, original)
                    || (typeVersion.fields.size() > 0 || typeVersion.methods.size() > 0)) {
                filtered.put(types.getKey(), typeVersion);
            }
        }

        final StringBuilder builder = new StringBuilder();

        for (String type : sorted(filtered.keySet())) {
            builder.setLength(0);
            builder.append("```diff\n");

            final ApiInfoStore.TypeVersion typeVersion = filtered.get(type);
            appendDiffed(builder, version, typeVersion);
            builder
                    .append(type)
                    .append('\n');

            final Map<String, ApiInfo> fields = typeVersion.fields;
            final Map<String, ApiInfo> methods = typeVersion.methods;

            for (String field : sorted(fields.keySet())) {
                if (appendDiffed(builder, version, fields.get(field))) {
                    builder.append("   ")
                            .append(field)
                            .append("\n");
                }
            }

            for (String method : sorted(methods.keySet())) {
                if (appendDiffed(builder, version, methods.get(method))) {
                    builder.append("   ")
                            .append(method)
                            .append("\n");
                }
            }

            builder.append("```\n\n");
            System.out.println(builder);
        }
    }

    private static boolean shouldEmit(@Nonnull ApiVersion version, @Nonnull ApiInfo info) {
        return version == info.since || version == info.deprecated;
    }

    private static List<String> sorted(@Nonnull Collection<String> collection) {
        final List<String> list = new ArrayList<>(collection);
        Collections.sort(list);
        return list;
    }

    private static boolean appendDiffed(
            @Nonnull StringBuilder builder,
            @Nonnull ApiVersion version,
            @Nonnull ApiInfo info) {

        // priority for deprecated (some nodes are both added and deprecated in the same version)

        boolean result = false;

        if (version == info.deprecated) {
            builder.append('-');
            result = true;
        }

        if (version == info.since) {
            builder.append('+');
            result = true;
        }

        return result;
    }

    private Stats() {
    }
}
