package ru.noties.enhance;

import com.github.javaparser.JavaParser;
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
import org.apache.commons.io.FileUtils;
import ru.noties.enhance.options.SourceFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static ru.noties.enhance.Log.log;

class EnhanceWriterImpl extends EnhanceWriter {

    private interface SourceFormatter {
        @Nonnull
        String format(@Nonnull String source);
    }

    private final SourceFormatter sourceFormatter;
    private final ApiInfoStore apiInfoStore;
    private final ApiVersionFormatter apiVersionFormatter;

    EnhanceWriterImpl(@Nonnull SourceFormat format, @Nonnull ApiInfoStore apiInfoStore, @Nonnull ApiVersionFormatter apiVersionFormatter) {
        this.sourceFormatter = sourceFormatter(format);
        this.apiInfoStore = apiInfoStore;
        this.apiVersionFormatter = apiVersionFormatter;
    }

    @Override
    public void write(@Nonnull File source, @Nonnull File destination) {

        final File[] files = source.listFiles();
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

                write(file, folder);

            } else {

                final String name = file.getName();
                final File f = new File(destination, name);

                if (name.endsWith(".java")) {

                    final String java = processJavaFile(file);
                    try {
                        FileUtils.write(f, java, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    try {
                        FileUtils.copyFile(file, f);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Nonnull
    private String processJavaFile(@Nonnull File file) {

        log("[Enhance] processing java source file: %s", file.getPath());

        final CompilationUnit unit;
        try {
            unit = JavaParser.parse(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        unit.accept(new ApiInfoVisitor(apiVersionFormatter), apiInfoStore);

        final String out;

        if (sourceFormatter == null) {
            out = unit.toString();
        } else {
            out = sourceFormatter.format(unit.toString());
        }

        return out;
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
                TypeDeclaration parent = parentTypeDeclaration(typeDeclaration);
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
        private static TypeDeclaration parentTypeDeclaration(@Nonnull TypeDeclaration typeDeclaration) {
            return (TypeDeclaration) typeDeclaration.getParentNode()
                    .filter(node -> node instanceof TypeDeclaration)
                    .orElse(null);
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
