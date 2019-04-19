package com.maxmin.tda.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class ObjectMapperFactory {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    public static ObjectMapper getInstance() {
        return objectMapper;
    }

    public static ObjectMapper buildFilterObjectMapper(final String... filters) {
        final ObjectMapper copyForFilter = objectMapper.copy();
        copyForFilter.addMixIn(Object.class, FilterMixin.class);
        copyForFilter.setFilterProvider(buildFilterProvider(filters));
        return copyForFilter;
    }

    private static SimpleFilterProvider buildFilterProvider(final String... filters) {
        return new SimpleFilterProvider().addFilter(Constant.FIELDS_FILTER, new PropertyFilter(filters));
    }
}