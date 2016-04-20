/*
 * MIT License
 *
 * Copyright (c) 2016  Altisource
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.ecloudmanager.deployment.core;

public class ConstraintField {

    private String name;
    private String description;
    private String defaultValue;
    private Type type;
    private boolean required;
    private boolean readOnly = false;
    private boolean allowReference = true;
    private ConstraintFieldSuggestionsProvider suggestionsProvider;

    public ConstraintField() {
    }
    private ConstraintField(String name, String description, String defaultValue, Type type, boolean required,
                            boolean readOnly, boolean allowReference, ConstraintFieldSuggestionsProvider
                                suggestionsProvider) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.type = type;
        this.required = required;
        this.readOnly = readOnly;
        this.allowReference = allowReference;
        this.suggestionsProvider = suggestionsProvider;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public Type getType() {
        return type;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isAllowReference() {
        return allowReference;
    }

    public ConstraintFieldSuggestionsProvider getSuggestionsProvider() {
        return suggestionsProvider;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (description != null) {
            sb.append(description);
        }
        if (name != null && !name.isEmpty()) {
            sb.append(" [").append(name).append("] ");
        }
        if (isRequired()) {
            sb.append("*");
        }
        return sb.toString();
    }

    public ConstraintField copy() {
        ConstraintField copy = new ConstraintField();
        copy.name = this.name;
        copy.description = this.description;
        copy.defaultValue = this.defaultValue;
        copy.type = this.type;
        copy.required = this.required;
        copy.readOnly = this.readOnly;
        copy.allowReference = this.allowReference;
        copy.suggestionsProvider = this.suggestionsProvider;
        return copy;
    }

    public enum Type {
        STRING,
        NUMBER
    }

    public static class Builder {

        private String name;
        private String description = "";
        private String defaultValue = null;
        private Type type = Type.STRING;
        private boolean required = true;
        private boolean readOnly = false;
        private boolean allowReference = true;
        private ConstraintFieldSuggestionsProvider suggestionsProvider = null;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        public Builder readOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public Builder allowReference(boolean allowReference) {
            this.allowReference = allowReference;
            return this;
        }

        public Builder suggestionsProvider(ConstraintFieldSuggestionsProvider suggestionsProvider) {
            this.suggestionsProvider = suggestionsProvider;
            return this;
        }

        public ConstraintField build() {
            return new ConstraintField(name, description, defaultValue, type, required, readOnly, allowReference,
                suggestionsProvider);
        }
    }
}
