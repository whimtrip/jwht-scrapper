package fr.whimtrip.ext.jwhtscrapper.service.holder;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 28/07/18</p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class Field {

    private final String name;

    private final String value;

    public Field(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {

        return name;
    }

    public String getValue() {

        return value;
    }
}
