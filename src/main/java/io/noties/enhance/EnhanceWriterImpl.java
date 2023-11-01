package io.noties.enhance;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import io.noties.enhance.options.SourceFormat;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.noties.enhance.Log.log;

class EnhanceWriterImpl extends EnhanceWriter {

    private interface Parser {
        @Nonnull
        CompilationUnit parse(@Nonnull File file);
    }

    private interface SourceFormatter {
        @Nonnull
        String format(@Nonnull String source);
    }

    @Nonnull
    private final Parser parser;

    @Nullable
    private final SourceFormatter sourceFormatter;

    @Nonnull
    private final ApiInfoStore apiInfoStore;

    @Nonnull
    private final ApiVersionFormatter apiVersionFormatter;

    EnhanceWriterImpl(
            int sdk,
            @Nonnull SourceFormat format,
            @Nonnull ApiInfoStore apiInfoStore,
            @Nonnull ApiVersionFormatter apiVersionFormatter
    ) {
        this.parser = sdk >= Api.SDK_34.sdkInt ? new Parser17() : new Parser11();

        this.sourceFormatter = sourceFormatter(format);
        this.apiInfoStore = apiInfoStore;
        this.apiVersionFormatter = apiVersionFormatter;
    }

    @Override
    public void write(@Nonnull File source, @Nonnull File destination) {
        write("", source, destination);
    }

    // `path` could be used in future if some files would be processed differently
    private void write(
            @Nonnull String path,
            @Nonnull File source,
            @Nonnull File destination
    ) {
        final File[] files = source.listFiles();
        //noinspection RedundantLengthCheck
        if (files == null
                || files.length == 0) {
            return;
        }

        for (File file : files) {

            if (file.isDirectory()) {

                final File folder = new File(destination, file.getName());
                if (!folder.mkdirs()) {
                    throw new RuntimeException("Cannot create folder: " + folder.getPath());
                }

                write(
                        path + "/" + file.getName(),
                        file,
                        folder
                );

            } else {

                final String name = file.getName();
                final File f = new File(destination, name);

                log("[Enhance] path:'%s' name:'%s'", path, name);

                if (isJavaFileToProcess(name)) {
                    final String java = processJavaFile(file);
                    try {
                        FileUtils.write(f, java, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        throw new RuntimeException(
                                "Error writing file:'" + name + "' at path:'" + path + "'",
                                e
                        );
                    }
                } else {
                    log("[Enhance] copy file: %s", file.getPath());
                    try {
                        FileUtils.copyFile(file, f);
                    } catch (IOException e) {
                        throw new RuntimeException(
                                "Error copying file:'" + name + "' at path:'" + path + "'",
                                e
                        );
                    }
                }
            }
        }
    }

    private boolean isJavaFileToProcess(@Nonnull String name) {
        // @since 1.0.3 there are also `*.annotated.java` files, ignore them
        return name.endsWith(".java") && !name.endsWith(".annotated.java");
    }

    @Nonnull
    private String processJavaFile(@Nonnull File file) {

        log("[Enhance] processing java source file: %s", file.getPath());

        final CompilationUnit unit = parser.parse(file);

        unit.accept(new ApiInfoVisitor(apiVersionFormatter), apiInfoStore);

        final String out;

        if (sourceFormatter == null) {
            out = unit.toString();
        } else {
            final String source = unit.toString();
            try {
                out = sourceFormatter.format(source);
            } catch (Throwable t) {
                try {
                    final File failedFile = new File(".", ".failed." + file.getName());
                    FileUtils.write(failedFile, source, "utf-8");
                } catch (IOException e) {
                    // ignored
                }
                throw t;
            }
        }

        return out;
    }

    @Nonnull
    private static CompilationUnit compile(@Nonnull JavaParser javaParser, @Nonnull File file) {
        final CompilationUnit unit;
        try {
            final ParseResult<CompilationUnit> result = javaParser.parse(file);
            if (result.isSuccessful()) {
                //noinspection OptionalGetWithoutIsPresent
                unit = result.getResult().get();
            } else {
                throw new RuntimeException(result.toString());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return unit;
    }

    private static class ApiInfoVisitor extends VoidVisitorAdapter<ApiInfoStore> {

        private final ApiVersionFormatter formatter;

        private String currentPackage;

        ApiInfoVisitor(@Nonnull ApiVersionFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public void visit(PackageDeclaration n, ApiInfoStore arg) {
            currentPackage = n.getNameAsString().replaceAll("\\.", "/") + "/";
            super.visit(n, arg);
        }

        @Override
        public void visit(EnumDeclaration n, ApiInfoStore arg) {
            super.visit(n, arg);

            final String type = typeName(n);

            final NodeList<EnumConstantDeclaration> constants = n.getEntries();
            if (constants != null) {
                for (EnumConstantDeclaration declaration : constants) {
                    setApiInfo(declaration, arg.field(type, declaration.getNameAsString()));
                }
            }

            visit(type, n, arg, n.getConstructors());
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, ApiInfoStore api) {
            super.visit(n, api);

            final String type = typeName(n);

            visit(type, n, api, n.getConstructors());
        }

        private void visit(
                @Nonnull String type,
                @Nonnull TypeDeclaration<?> n,
                @Nonnull ApiInfoStore api,
                @Nullable List<ConstructorDeclaration> constructors
        ) {

            final List<FieldDeclaration> fields = n.getFields();
            if (fields != null) {

                String name;

                for (FieldDeclaration field : fields) {

                    name = field.getVariables().get(0).getNameAsString();
                    setApiInfo(field, api.field(type, name));
                }
            }

            final List<CallableDeclaration<?>> callableDeclarations;
            {
                callableDeclarations = new ArrayList<>();
                final List<MethodDeclaration> methods = n.getMethods();
                if (methods != null) {
                    callableDeclarations.addAll(methods);
                }

                if (constructors != null) {
                    callableDeclarations.addAll(constructors);
                }
            }

            for (CallableDeclaration<?> declaration : callableDeclarations) {
                setApiInfo(declaration, api.method(type, ByteCodeSignature.create(declaration)));
            }

            final ApiInfo info = api.type(type);
            if (info != null) {
                setApiInfo(n, info);
            }
        }

        private void setApiInfo(@Nonnull NodeWithJavadoc<?> node, @Nullable ApiInfo apiInfo) {

            if (apiInfo == null) {
                return;
            }

            Javadoc javadoc = node.getJavadoc().orElse(null);
            if (javadoc == null) {
                javadoc = new Javadoc(new JavadocDescription());
            }
            if (apiInfo.since != null) {
                javadoc.addBlockTag("since", formatter.format(apiInfo.since));
            }
            if (apiInfo.deprecated != null) {
                javadoc.addBlockTag("deprecated", formatter.format(apiInfo.deprecated));
            }
            node.setJavadocComment(javadoc.toComment("  "));
        }

        @Nonnull
        private String typeName(@Nonnull TypeDeclaration<?> typeDeclaration) {
            final String out;
            if (typeDeclaration.isTopLevelType()) {
                out = currentPackage + typeDeclaration.getNameAsString();
            } else {
                final StringBuilder builder = new StringBuilder();
                builder.append(typeDeclaration.getNameAsString());
                TypeDeclaration<?> parent = parentTypeDeclaration(typeDeclaration);
                while (parent != null) {
                    builder.insert(0, '$');
                    builder.insert(0, parent.getNameAsString());
                    parent = parentTypeDeclaration(parent);
                }
                builder.insert(0, currentPackage);
                out = builder.toString();
            }
            return out;
        }

        @Nullable
        private static TypeDeclaration<?> parentTypeDeclaration(@Nonnull TypeDeclaration<?> typeDeclaration) {
            return (TypeDeclaration<?>) typeDeclaration.getParentNode()
                    .filter(node -> node instanceof TypeDeclaration)
                    .orElse(null);
        }
    }

    // Unfortunately java-parser printer is a little weird and does not give enough options
    //  to format the code
//    @Nonnull
//    private static Printer createDefaultPrinter(int indent) {
//        final PrinterConfiguration configuration = new DefaultPrinterConfiguration()
//                .addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.INDENTATION, new Indentation(Indentation.IndentType.SPACES, indent)))
//                .addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.ORDER_IMPORTS, Boolean.TRUE))
//                .addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.SORT_IMPORTS_STRATEGY, new IntelliJImportOrderingStrategy()))
//                .addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS, Boolean.TRUE))
//                .addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_JAVADOC, Boolean.TRUE))
//                .addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.COLUMN_ALIGN_PARAMETERS, Boolean.FALSE))
//                .addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.COLUMN_ALIGN_FIRST_METHOD_CHAIN, Boolean.FALSE))
//                .addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.INDENT_CASE_IN_SWITCH, Boolean.FALSE))
//                .addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.MAX_ENUM_CONSTANTS_TO_ALIGN_HORIZONTALLY, 1));
//        return new DefaultPrettyPrinter(configuration);
//    }

    private static class Parser11 implements Parser {

        private final JavaParser javaParser11 = new JavaParser(new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_11));

        @Nonnull
        @Override
        public CompilationUnit parse(@Nonnull File file) {
            final CompilationUnit unit;
            try {
                final ParseResult<CompilationUnit> result = javaParser11.parse(file);
                if (result.isSuccessful()) {
                    //noinspection OptionalGetWithoutIsPresent
                    unit = result.getResult().get();
                } else {
                    throw new RuntimeException(result.toString());
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            return unit;
        }
    }

    // Android 34 should have been compiled with Java-17, but some sources
    //  contain java-17 keywords: `sealed` and `permits` as variable names
    private static class Parser17 extends Parser11 {

        private final JavaParser javaParser17 = new JavaParser(new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17));

        @Nonnull
        @Override
        public CompilationUnit parse(@Nonnull File file) {

            // first try parsing with java-17 and then fallback to java-11
            //  this is done because, even though android-34 should be compiled with java-17
            //  there are classes that contain illegal variable names: `sealed` and `permits`
            CompilationUnit compilationUnit = null;
            try {
                compilationUnit = compile(javaParser17, file);
            } catch (Throwable t) {
                log("[Enhance] Exception parsing with java-17");
                //noinspection CallToPrintStackTrace
                t.printStackTrace();
            }

            if (compilationUnit == null) {
                compilationUnit = super.parse(file);
            }

            return compilationUnit;
        }
    }

    @Nullable
    private static SourceFormatter sourceFormatter(@Nonnull SourceFormat format) {

        final SourceFormatter sourceFormatter;

        switch (format) {

            case AOSP:
                sourceFormatter = new AospSourceFormatter();
                break;

            case GOOGLE:
                sourceFormatter = new GoogleSourceFormatter();
                break;

            default:
                sourceFormatter = null;
        }

        return sourceFormatter;
    }

    private static class AospSourceFormatter implements SourceFormatter {

        private final Formatter formatter;

        AospSourceFormatter() {
            final JavaFormatterOptions options = JavaFormatterOptions.builder()
                    .style(JavaFormatterOptions.Style.AOSP)
                    .build();
            formatter = new Formatter(options);
        }

        @Nonnull
        @Override
        public String format(@Nonnull String source) {
            try {
                return formatter.formatSource(source);
            } catch (FormatterException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class GoogleSourceFormatter implements SourceFormatter {

        private final Formatter formatter;

        GoogleSourceFormatter() {
            final JavaFormatterOptions options = JavaFormatterOptions.builder()
                    .style(JavaFormatterOptions.Style.GOOGLE)
                    .build();
            formatter = new Formatter(options);
        }

        @Nonnull
        @Override
        public String format(@Nonnull String source) {
            try {
                return formatter.formatSource(source);
            } catch (FormatterException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
