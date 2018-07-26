package fr.whimtrip.ext.jwhtscrapper.intfr;

import java.io.IOException;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public interface BasicObjectMapper {

    <U> U readValue(String htmlResponse, Class<U> mappedClazz) throws IOException;

    <U> U readValue(String htmlResponse, Class<U> mappedClazz, U obj) throws IOException;
}
