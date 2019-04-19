package com.maxmin.tda.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PropertyFilter extends SimpleBeanPropertyFilter {

    private final Set<String> propertiesToInclude;

    private final Map<String, Boolean> matchCache = new HashMap<>();

    public PropertyFilter(final String... properties) {
        super();
        this.propertiesToInclude = new HashSet<>(properties.length);
        this.propertiesToInclude.addAll(Arrays.asList(properties));
    }


    private String getPathToTest(final PropertyWriter writer, final JsonGenerator jgen) {
        final StringBuilder nestedPath = new StringBuilder();
        nestedPath.append(writer.getName());
        JsonStreamContext sc = jgen.getOutputContext();
        if (sc != null) {
            sc = sc.getParent();
        }
        while (sc != null) {
            if (sc.getCurrentName() != null) {
                if (nestedPath.length() > 0) {
                    nestedPath.insert(0, Constant.DOT);
                }
                nestedPath.insert(0, sc.getCurrentName());
            }
            sc = sc.getParent();
        }
        return nestedPath.toString();
    }

    private boolean include(final PropertyWriter writer, final JsonGenerator jgen) {
        final String pathToTest = getPathToTest(writer, jgen);

        if (this.matchCache.containsKey(pathToTest)) {
            return this.matchCache.get(pathToTest);
        }

        final boolean include = this.propertiesToInclude.stream()
                .anyMatch(p -> matchPath(pathToTest, p));
        this.matchCache.put(pathToTest, include);
        return include;
    }


    private boolean matchPath(final String pathToTest, final String pattern) {
        return pattern.equals(pathToTest);
    }

    @Override
    public void serializeAsField(final Object pojo, final JsonGenerator jgen, final SerializerProvider provider,
                                 final PropertyWriter writer) throws Exception {
        if (include(writer, jgen)) {
            writer.serializeAsField(pojo, jgen, provider);
        }
    }
}