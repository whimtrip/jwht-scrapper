package fr.whimtrip.ext.jwhtscrapper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.BasicObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     Default implementation of an {@link BasicObjectMapper}. It allows to parse
 *     JSON strings to Java POJO when using Jackson's annotations on your POJO.
 *     It is a simple wrapper that uses under the hood a Jackson {@link ObjectMapper}.
 *
 *     You can provide your own ObjectMapper with your own parameters using the below
 *     constructor.
 *
 *     Otherwise a application global default Object Mapper will used.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public final class JacksonBasicObjectMapper implements BasicObjectMapper {

    private static ObjectMapper staticObjectMapper;
    private final ObjectMapper objectMapper;


    /**
     * Default constructor that will use default {@link ObjectMapper}
     * implementation as an application global Object Mapper.
     */
    public JacksonBasicObjectMapper() {
        this(null);
    }

    /**
     * Secondary Constructor to provide your own Object Mapper.
     * @param objectMapper your own parametizered {@link ObjectMapper}
     */
    public JacksonBasicObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public <U> U readValue(@NotNull String rawBody, @NotNull Class<U> mappedClazz) throws IOException {
        return getObjectMapper().readValue(rawBody, mappedClazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <U> U readValue(@NotNull String rawBody, @NotNull Class<U> mappedClazz, @NotNull U obj) throws IOException {
        return getObjectMapper().readerForUpdating(obj).readValue(rawBody);
    }

    /**
     * @return the object mapper to use for this basic object mapper. If provided object mapper
     *         is null, JVM-wide static one will be used (and eventually instanciated if not
     *         already).
     */
    private ObjectMapper getObjectMapper() {
        if(objectMapper != null)
            return objectMapper;
        else if (staticObjectMapper == null)
            staticObjectMapper = new ObjectMapper();
        return staticObjectMapper;
    }
}
