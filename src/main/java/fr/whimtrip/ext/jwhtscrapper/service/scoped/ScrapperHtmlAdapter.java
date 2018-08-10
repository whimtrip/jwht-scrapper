package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.core.util.WhimtripUtils;
import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoEngine;
import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoUtils;
import fr.whimtrip.ext.jwhthtmltopojo.adapter.DefaultHtmlAdapterImpl;
import fr.whimtrip.ext.jwhthtmltopojo.adapter.HtmlToPojoAnnotationMap;
import fr.whimtrip.ext.jwhthtmltopojo.annotation.Selector;
import fr.whimtrip.ext.jwhthtmltopojo.exception.FieldShouldNotBeSetException;
import fr.whimtrip.ext.jwhthtmltopojo.exception.ParseException;
import fr.whimtrip.ext.jwhthtmltopojo.intrf.HtmlAdapter;
import fr.whimtrip.ext.jwhthtmltopojo.intrf.HtmlField;
import fr.whimtrip.ext.jwhtscrapper.annotation.*;
import fr.whimtrip.ext.jwhtscrapper.exception.WarningSignException;
import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.whimtrip.ext.jwhtscrapper.enm.TriggeredOn.*;


/**
 *
 *
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     This class extends the {@link DefaultHtmlAdapterImpl} to provide
 *     a specific implementation of {@link HtmlAdapter} from the htmltopojo
 *     lib from fr.whimtrip. This allows us to build custom annotation
 *     processing on top of implementations provided by default
 *     {@link DefaultHtmlAdapterImpl} adapter. Features added is support
 *     for link following and warning sign triggering.
 * </p>
 * <p>New annotations supported with this implementation are :</p>
 * <ul>
 *     <li>{@link Link}</li>
 *     <li>{@link LinkObject}</li>
 *     <li>{@link LinkObjects}</li>
 *     <li>{@link WarningSign}</li>
 * </ul>
 * <p>New Annotations that are handled and processed elsewhere :</p>
 * <ul>
 *     <li>{@link Field}</li>
 *     <li>{@link LinkListsFromBuilder}</li>
 * </ul>
 *
 *
 * @author Louis-wht
 * @since 1.0.0
 * @param <T> T is the model on which the Html responses will be mapped
 */
public class ScrapperHtmlAdapter<T> extends DefaultHtmlAdapterImpl<T> {


    public ScrapperHtmlAdapter(HtmlToPojoEngine htmlToPojoEngine, Class<T> clazz) {
        super(htmlToPojoEngine, clazz);
    }

    /**
     * <p>
     *     This method features a major difference from the classic
     *     implementation. Before calling {@link HtmlField#setFieldOrThrow(Object, Object)}
     *     warning signs are checked {@link #checkAndThrowWarningSign(java.lang.reflect.Field, Object)}.
     *     When warning signs are detected an {@link WarningSignException}
     *     will be triggered and handled by the calling stack.
     * </p>
     *
     * @see HtmlAdapter#loadFromNode(Element, Object)})
     * @param node {@link HtmlAdapter#loadFromNode(Element, Object)})}
     * @param newInstance {@link HtmlAdapter#loadFromNode(Element, Object)})}
     * @return {@link HtmlAdapter#loadFromNode(Element, Object)})}
     */
    @Override
    public T loadFromNode(Element node, T newInstance) {

        for (HtmlField<T> htmlField : htmlFieldCache.values()) {

            Object fieldValue = null;
            boolean shouldFieldBeSet = true;
            try {
                fieldValue = htmlField.getRawValue(htmlToPojoEngine, node, newInstance);
            }
            catch (FieldShouldNotBeSetException e) {
                shouldFieldBeSet = false;
            }

            if(shouldFieldBeSet)
            {
                checkAndThrowWarningSign(htmlField.getField(), fieldValue);

                htmlField.setFieldOrThrow(newInstance, fieldValue);
            }
        }
        return newInstance;

    }

    /**
     * <p>This implementation adds a new method on standard {@link HtmlAdapter}.</p>
     * <p>
     *     This method will help find the field to assign the new POJO
     *     from the new link to poll. For more details, see {@link Link}
     *     and {@link LinkObject} / {@link LinkObjects}.
     * </p>
     * @param link the link {@link HtmlToPojoAnnotationMap} we need to
     *             find the field to set the new POJO to for.
     * @return the field we will set the resulting POJO to out of the
     *         {@link Link} url scrapped.
     */
    public HtmlToPojoAnnotationMap getLinkObject(HtmlToPojoAnnotationMap<Link> link)
    {

        List<? extends HtmlToPojoAnnotationMap<LinkObject>> linkObjectFields = getFieldList(LinkObject.class);
        if(linkObjectFields != null)
        {
            for (HtmlToPojoAnnotationMap<LinkObject> linkObject : linkObjectFields)
            {
                if(linkObject.getAnnotation().value().equals(link.getName()))
                    return linkObject;
            }
        }

        List<? extends HtmlToPojoAnnotationMap<LinkObjects>> linkObjectsFields = getFieldList(LinkObjects.class);
        if(linkObjectsFields != null)
        {
            for(HtmlToPojoAnnotationMap<LinkObjects> linkObjects : linkObjectsFields)
            {
                if(   Arrays.asList(linkObjects.getAnnotation().value()).contains(link.getName())
                   && Collection.class.isAssignableFrom(linkObjects.getField().getType())   )
                    return linkObjects;
            }
        }

        return null;
    }


    /**
     * <p>
     *     Simplified method for warning sign checking calling
     *     {@link #isWarningSignTriggered(java.lang.reflect.Field, Object, WarningSign)}
     *     internally to detect warning signs.
     * </p>
     * @param field the field to analyse warning sign for
     * @param value the raw value to test wether it is a warning sign or not.
     * @throws WarningSignException if the value was detected to be a Warning Sign.
     */
    private void checkAndThrowWarningSign(java.lang.reflect.Field field, Object value) throws WarningSignException {
        WarningSign warningSign;
        if((warningSign = field.getAnnotation(WarningSign.class)) != null)
        {
            if( isWarningSignTriggered(field, value, warningSign))
                throw new WarningSignException(field);
        }
    }

    /**
     *
     * <p>Core method to know if a warning sign was detected</p>
     * @param field the field to analyse warning sign for
     * @param value the original value to test wether it is a warning sign or not.
     * @param warningSign {@link WarningSign} annotation found on top of
     *                    the field to analyse.
     * @return a boolean indicating wether a warning sign was or wasn't detected.
     * @throws WarningSignException if the value was detected to be a Warning Sign.
     */
    private boolean isWarningSignTriggered(java.lang.reflect.Field field, Object value, WarningSign warningSign) {

        if(        warningSign.triggeredOn() != ANY_VALUE_MATCHING_REGEX
                && warningSign.triggeredOn() != ANY_VALUE_NOT_MATCHING_REGEX)
        {

            return
                    (warningSign.triggeredOn() == NULL_VALUE && isNullOrEquivalent(value))
                 || (warningSign.triggeredOn() == DEFAULT_VALUE && isEqualToDefaultValue(field, value))
                 || (warningSign.triggeredOn() == ANY_CORRECT_VALUE && !isNullOrEquivalent(value) && !isEqualToDefaultValue(field, value));
        }

        else if(value instanceof String)
        {
            Pattern pattern = Pattern.compile(warningSign.triggeredOnRegex());
            Matcher m = pattern.matcher((String)value);
            boolean match = m.find();
            return
                    warningSign.triggeredOn() == ANY_VALUE_NOT_MATCHING_REGEX && !match
                 || warningSign.triggeredOn() == ANY_VALUE_MATCHING_REGEX && match;
        }

        return false;
    }

    /**
     * @param field the field to analyse
     * @param value the original value retrieved from the field
     * @return a boolean indicating wether it is a default value or not
     */
    private boolean isEqualToDefaultValue(java.lang.reflect.Field field, Object value) {
        Object castedDefaultValue = buildCastedDefaultValue(field);
        return    isEqualAsNumbers(value, castedDefaultValue)
                || value == castedDefaultValue
                || (value != null && value.equals(castedDefaultValue));
    }

    /**
     * @param value the original value retrieved from the field
     * @param castedDefaultValue the casted default value
     * @return a boolean indicating if they are equals as numbers
     */
    private boolean isEqualAsNumbers(Object value, Object castedDefaultValue) {

        return     value instanceof Number && castedDefaultValue instanceof  Number
                && WhimtripUtils.compareNumbers((Number) value, (Number) castedDefaultValue) == 0;
    }

    /**
     * @param value the value to analyse
     * @return a boolean indicating wether it is considered as a
     *         null value or not
     */
    private boolean isNullOrEquivalent(Object value) {
        return value == null || (value instanceof String && ((String) value).isEmpty())
                || (value instanceof List && ((List) value).isEmpty());
    }

    /**
     * @param field the field to build default value from
     * @return the casted default value
     */
    private Object buildCastedDefaultValue(java.lang.reflect.Field field) {

        Selector selector = field.getAnnotation(Selector.class);
        Object castedDefaultValue = null;
        try {
            castedDefaultValue = HtmlToPojoUtils.castValue(
                    selector.defValue(),
                    field.getType(),
                    "",
                    selector.locale().equals(Selector.NO_VALUE) ?
                            Locale.getDefault() : Locale.forLanguageTag(selector.locale())
            );
        }catch(IllegalArgumentException e)
        {
            throw new ParseException(selector.defValue(), Locale.getDefault(), field);
        }
        return castedDefaultValue;
    }

}
