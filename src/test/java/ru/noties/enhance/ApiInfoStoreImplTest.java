package ru.noties.enhance;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static ru.noties.enhance.ApiInfoStoreImpl.Parser.normalizeMethodSignature;

public class ApiInfoStoreImplTest {

    @Test
    public void test() {

        final Map<String, String> map = new HashMap<String, String>() {{
            put("instantiateClassLoader(Ljava/lang/ClassLoader;Landroid/content/pm/ApplicationInfo;)Ljava/lang/ClassLoader;", "instantiateClassLoader(LClassLoader;LApplicationInfo;)LClassLoader;");
            put("setSingleChoiceItems([Ljava/lang/CharSequence;ILandroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;", "setSingleChoiceItems([LCharSequence;ILOnClickListener;)LBuilder;");
            put("readFloat()F", "readFloat()F");
            put("readDoubleArray([D)V", "readDoubleArray([D)V");
            put("obtain(I)Landroid/os/Parcel;", "obtain(I)LParcel;");
            put("isPseudoLocale(Landroid/icu/util/ULocale;)Z", "isPseudoLocale(LULocale;)Z");
            put("readParcelableList(Ljava/util/List;Ljava/lang/ClassLoader;)Ljava/util/List;", "readParcelableList(LList;LClassLoader;)LList;");
        }};

        for (Map.Entry<String, String> entry : map.entrySet()) {
            assertEquals(entry.getKey(), entry.getValue(), normalizeMethodSignature(entry.getKey()));
        }
    }
}