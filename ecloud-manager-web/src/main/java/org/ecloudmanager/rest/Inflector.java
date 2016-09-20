package org.ecloudmanager.rest;

import io.swagger.inflector.SwaggerInflector;
import io.swagger.inflector.config.Configuration;
import io.swagger.util.Yaml;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.io.IOException;

public class Inflector extends SwaggerInflector {
    public Inflector(@Context ServletContext ctx) throws IOException {
        super(Yaml.mapper().readValue(Inflector.class.getClassLoader().getResourceAsStream("inflector.yaml"), Configuration.class));
    }
}
