package org.ecloudmanager.node;

import org.ecloudmanager.node.model.LoggingEvent;

import java.util.Collection;

public interface LoggingEventListener {
    void log(Collection<LoggingEvent> events);
}
