# Enhance!

<img src="art/logo.png" />

Command line utility to process Android source files distributed via SDK manager and add API version information as javadoc tags:

```java
/**
 * Called by the system when the activity changes from fullscreen mode to multi-window mode and
 * visa-versa.
 *
 * @see android.R.attr#resizeableActivity
 * @param isInMultiWindowMode True if the activity is in multi-window mode.
 * @deprecated Use {@link #onMultiWindowModeChanged(boolean, Configuration)} instead.
 * @since 7.0 Nougat (24)
 * @deprecated 8.0 Oreo (26)
 */
@Deprecated
public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
    // Left deliberately empty. There should be no side effects if a direct
    // subclass of Activity does not call super.
}
```

Ironically allows to **actually format** processed code with **AOSP** code style specification (and as [google-java-format](https://github.com/google/google-java-format) is used - `GOOGLE` style is also supported).

---

Pick the `jar` file from the latest [release](https://github.com/noties/Enhance/releases/latest/).

There are few configuration options:
* `sdk`: (required) Android SDK version (for example 25)
* `format`: Allows to format processed Java source files. Available options are: `aosp` and `google`. Everything else (including empty argument) won't format processed code
* `sp`: path to Android SDK
* `diff`: just generate statistics info/diff for specified SDK version

```
usage: Enhance
 -diff                  Emit diff
 -format <arg>          Format sources. Accepts (aosp|google). Everything
                        else would keep original formatting
 -h,--help              Prints help
 -sdk <arg>             Specify which SDK version to process.
 -sp,--sdk-path <arg>   Path to Android SDK. If not specified
                        'ANDROID_HOME' system variable will be used
```

Please note that you Android SDK folder must already contain sources for specified `sdk` version.

So, usage would be like that:

```bash
# just add api information to source code, no formatting
java -jar enhance.jar -sdk 26

# also format with AOSP
java -jar enhance.jar -sdk 26 -format aosp

# or GOOGLE
java -jar enhance.jar -sdk 26 -format google

# or with custom SDK path
java -jar enhance.jar -sdk 26 -sp "/Users/not_me/android/sdk"
```

If you would like to restore unmodified copy of source code you can find it: `{your-home-directory}/.enhance-backup/android-{sdk}`

## Formatting on JDK 17
Formatting is done with the [google-java-format](https://github.com/google/google-java-format) library
which requires access to the internals of the JDK. This is why on JDK-17 in order to format
the sources additional commandline arguments are required:

```bash
java \
  --add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED \
  --add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
  --add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED \
  --add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED \
  --add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
  --add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED \
  -jar enhance-34-all.jar -sdk 34 -format google
```

## Thanks

Big kudos to the maintainers of amazing [javaparser](https://github.com/javaparser/javaparser)!

## License

```
  Copyright 2018 Dimitry Ivanov (legal@noties.io)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```