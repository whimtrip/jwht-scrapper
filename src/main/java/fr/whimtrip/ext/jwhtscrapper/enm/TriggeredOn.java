package fr.whimtrip.ext.jwhtscrapper.enm;

import fr.whimtrip.ext.jwhthtmltopojo.annotation.Selector;
import fr.whimtrip.ext.jwhtscrapper.annotation.WarningSign;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     This enumeration defines all possible triggering schemes
 *     for a warning sign.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 *
 */
public enum TriggeredOn {

    /**
     * When the field had a null or equivalent value. Null or Equivalent values
     * are : null, empty string or empty list.
     */
    NULL_VALUE,

    /**
     * When the field has the default value defined by {@link Selector#defValue()}.
     */
    DEFAULT_VALUE,

    /**
     * When the value is neither {@link #NULL_VALUE},
     * nor {@link #DEFAULT_VALUE}
     */
    ANY_CORRECT_VALUE,

    /**
     * When the value matches the regex given with {@link WarningSign#triggeredOnRegex()}.
     */
    ANY_VALUE_MATCHING_REGEX,


    /**
     * When the value doesn't match the regex given with {@link WarningSign#triggeredOnRegex()}.
     */
    ANY_VALUE_NOT_MATCHING_REGEX
}
