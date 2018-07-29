package fr.whimtrip.ext.jwhtscrapper.service.holder;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 28/07/18</p>
 *
 * <p>Field class for POST fields to use</p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class Field {


    private final String name;

    private final String value;

    /**
     * Default and only possible constructor.
     * @param name the name of the field
     * @param value its value.
     */
    public Field(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * @return the name of the field.
     */
    public String getName() {

        return name;
    }

    /**
     * @return the value of the field.
     */
    public String getValue() {

        return value;
    }
}
