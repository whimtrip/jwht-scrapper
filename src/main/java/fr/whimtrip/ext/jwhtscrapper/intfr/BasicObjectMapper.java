package fr.whimtrip.ext.jwhtscrapper.intfr;

import fr.whimtrip.ext.jwhtscrapper.service.JacksonBasicObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     Object Mapper simple implementation allowing to easily plug
 *     another mapper that could handle 100% different processing
 *     unit to convert objects to POJOs.
 * </p>
 *
 *
 * @see JacksonBasicObjectMapper Default Jackson JSON -> POJO implementation of
 *                               this interface.
 * @author Louis-wht
 * @since 1.0.0
 */
public interface BasicObjectMapper {

    /**
     * <p>Will read a value from raw stringified HTTP body given a POJO class.</p>
     * @param rawBody the raw HTTP stringified body to use to map it to a POJO instance.
     * @param mappedClazz the class to map the HTTP body to.
     * @param <U> the type of the class to map the HTTP body to.
     * @return the instanciated and mapped POJO instance.
     * @throws IOException if read operation cannot be properly performed.
     */
    <U> U readValue(@NotNull final String rawBody, @NotNull final Class<U> mappedClazz) throws IOException;

    /**
     * <p>
     *     Will read a value from raw stringified HTTP body given a POJO class.
     *     This method needs to deal with an existing, already instanciated POJO
     *     with possibly already existing default values.
     * </p>
     * @param rawBody the raw HTTP stringified body to use to map it to a POJO instance.
     * @param mappedClazz the class to map the HTTP body to.
     * @param <U> the type of the class to map the HTTP body to.
     * @return the mapped POJO instance. must be the same as the instance given as a
     *         parameter.
     * @throws IOException if read operation cannot be properly performed.
     */
    <U> U readValue(@NotNull final String rawBody, @NotNull final Class<U> mappedClazz, @NotNull final U obj) throws IOException;
}
