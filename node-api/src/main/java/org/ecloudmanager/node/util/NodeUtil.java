package org.ecloudmanager.node.util;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.ecloudmanager.node.NodeBaseAPI;
import org.ecloudmanager.node.model.APIInfo;
import org.ecloudmanager.node.model.LoggingEvent;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeUtil {
    private static ThrowableProxyConverter converter = new ThrowableProxyConverter();
    private static Logger log = LoggerFactory.getLogger(NodeUtil.class);

    public static LoggingEvent fromLogback(ILoggingEvent event) {
        return new LoggingEvent().level(event.getLevel().toString()).message(event.getFormattedMessage()).timeStamp(event.getTimeStamp()).throwable(converter.convert(event)).logger(event.getLoggerName());
    }

    public static Map<String, APIInfo> getAvailableAPIs() {
        List<Class<? extends NodeBaseAPI>> availableClasses;
        availableClasses = new Reflections("org.ecloudmanager.node").getSubTypesOf(NodeBaseAPI.class).stream()
                .filter(c -> !c.isInterface())
                .collect(Collectors.toList());
        Map<String, APIInfo> availableAPIs = new HashMap<>();
        availableClasses.forEach(c -> {
            try {
                availableAPIs.put(c.getName(), c.newInstance().getAPIInfo());
            } catch (Exception e) {
                log.error("Can't register node api from class " + c.getName(), e);
            }
        });
        return availableAPIs;
    }

}
