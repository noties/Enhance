package io.noties.enhance;

import javax.annotation.Nonnull;

public abstract class Log {

    public static void log(@Nonnull String msg, Object... args) {
        System.out.printf(msg, args);
        System.out.println();
    }

    private Log() {
    }
}
