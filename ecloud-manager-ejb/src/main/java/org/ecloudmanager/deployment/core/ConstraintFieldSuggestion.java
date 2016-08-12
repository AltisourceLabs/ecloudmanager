package org.ecloudmanager.deployment.core;

import java.util.List;
import java.util.stream.Collectors;

public class ConstraintFieldSuggestion {
    private final String value;
    private final String label;

    public ConstraintFieldSuggestion(String label, String value) {
        this.label = label != null ? label : value;
        this.value = value;
    }

    public ConstraintFieldSuggestion(String value) {
        this(null, value);
    }

    public static List<ConstraintFieldSuggestion> suggestionsList(List<String> stringList) {
        return stringList.stream().map(ConstraintFieldSuggestion::new).collect(Collectors.toList());
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}
