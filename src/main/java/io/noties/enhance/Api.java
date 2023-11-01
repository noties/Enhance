package io.noties.enhance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;

public enum Api {
    SDK_1(1, "1.0", "(initial)"),
    SDK_2(2, "1.1", "(initial)"),
    SDK_3(3, "1.5", "Cupcake"),
    SDK_4(4, "1.6", "Donut"),
    SDK_5(5, "2.0", "Eclair"),
    SDK_6(6, "2.0.1", "Eclair"),
    SDK_7(7, "2.1", "Eclair"),
    SDK_8(8, "2.2", "Froyo"),
    SDK_9(9, "2.3", "Gingerbread"),
    SDK_10(10, "2.3.3", "Gingerbread"),
    SDK_11(11, "3.0", "Honeycomb"),
    SDK_12(12, "3.1", "Honeycomb"),
    SDK_13(13, "3.2", "Honeycomb"),
    SDK_14(14, "4.0", "Ice Scream Sandwich"),
    SDK_15(15, "4.0.3", "Ice Scream Sandwich"),
    SDK_16(16, "4.1", "Jelly Bean"),
    SDK_17(17, "4.2", "Jelly Bean"),
    SDK_18(18, "4.3", "Jelly Bean"),
    SDK_19(19, "4.4", "Kitkat"),
    SDK_20(20, "4.4W", "Kitkat"),
    SDK_21(21, "5.0", "Lollipop"),
    SDK_22(22, "5.1", "Lollipop"),
    SDK_23(23, "6.0", "Marshmallow"),
    SDK_24(24, "7.0", "Nougat"),
    SDK_25(25, "7.1", "Nougat"),
    SDK_26(26, "8.0", "Oreo"),
    SDK_27(27, "8.1", "Oreo"),
    SDK_28(28, "9.0", "Pie"),
    SDK_29(29, "10", "Android Q"),
    SDK_30(30, "11", "Android R"),
    SDK_31(31, "12", "Android S"),
    SDK_32(32, "12", "Android S_V2"),
    SDK_33(33, "13", "Tiramisu"),
    SDK_34(34, "14", "Upside Down Cake")
    ;

    public final int sdkInt;
    public final String versionName;
    public final String codeName;

    Api(int sdkInt, @Nonnull String versionName, @Nonnull String codeName) {
        this.sdkInt = sdkInt;
        this.versionName = versionName;
        this.codeName = codeName;
    }

    @Override
    public String toString() {
        return "Api.SDK{" +
                "sdkInt=" + sdkInt +
                ", versionName='" + versionName + '\'' +
                ", codeName='" + codeName + '\'' +
                '}';
    }

    private static final List<Api> VALUES = List.of(values());

    @Nonnull
    public static Api latest() {
        return VALUES.get(VALUES.size() - 1);
    }

    @Nullable
    public static Api of(int sdkInt) {
        // NB! we assume that it starts at 1
        // so, SDK_1 is at 0, SDK_2 at 1, etc
        final int ordinal = sdkInt - 1;
        if (ordinal < 0 || ordinal >= VALUES.size()) {
            return null;
        }
        return VALUES.get(ordinal);
    }
}
