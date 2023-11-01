package io.noties.enhance;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

import javax.annotation.Nonnull;

public abstract class ByteCodeSignature {

    @Nonnull
    public static String create(@Nonnull CallableDeclaration<?> declaration) {
        return new Creator(declaration).get();
    }

    private ByteCodeSignature() {
    }

    private static class Creator {

        private final CallableDeclaration<?> declaration;

        private final StringBuilder builder = new StringBuilder();

        Creator(@Nonnull CallableDeclaration<?> declaration) {
            this.declaration = declaration;
            name();
            parameters();
            returnType();
        }

        @Nonnull
        String get() {
            return builder.toString();
        }

        private void name() {
            if (declaration.isConstructorDeclaration()) {
                builder.append("<init>");
            } else {
                builder.append(declaration.getNameAsString());
            }
        }

        private void parameters() {
            builder.append('(');
            for (Parameter parameter : declaration.getParameters()) {
                type(parameter.getType());
            }
            builder.append(')');
        }

        private void returnType() {
            if (declaration.isConstructorDeclaration()) {
                builder.append('V');
            } else {
                type(((MethodDeclaration) declaration).getType());
            }
        }

        private void type(@Nonnull Type type) {

            while (type.isArrayType()) {
                builder.append('[');
                type = ((ArrayType) type).getComponentType();
            }

            if (type.isVoidType()) {
                builder.append('V');
            } else if (type.isPrimitiveType()) {
                primitiveType((PrimitiveType) type);
            } else {
                classOrInterfaceType((ClassOrInterfaceType) type);
            }
        }

        private void primitiveType(@Nonnull PrimitiveType primitiveType) {
            switch (primitiveType.getType()) {

                case BOOLEAN:
                    builder.append('Z');
                    break;

                case CHAR:
                    builder.append('C');
                    break;

                case BYTE:
                    builder.append('B');
                    break;

                case SHORT:
                    builder.append('S');
                    break;

                case INT:
                    builder.append('I');
                    break;

                case LONG:
                    builder.append('J');
                    break;

                case FLOAT:
                    builder.append('F');
                    break;

                case DOUBLE:
                    builder.append('D');
                    break;
            }
        }

        // NB simplified signature here (no package info nor parent)
        private void classOrInterfaceType(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
            builder.append('L');

            String value = classOrInterfaceType.asString();

            final int index = value.lastIndexOf('.');
            if (index > -1) {
                value = value.substring(index + 1);
            }
            builder.append(value);
            builder.append(';');
        }
    }
}
