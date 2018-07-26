package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.core.util.WhimtripUtils;
import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoEngine;
import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoUtils;
import fr.whimtrip.ext.jwhthtmltopojo.adapter.DefaultHtmlAdapterImpl;
import fr.whimtrip.ext.jwhthtmltopojo.adapter.HtmlToPojoAnnotationMap;
import fr.whimtrip.ext.jwhthtmltopojo.annotation.Selector;
import fr.whimtrip.ext.jwhthtmltopojo.exception.FieldShouldNotBeSetException;
import fr.whimtrip.ext.jwhthtmltopojo.exception.ParseException;
import fr.whimtrip.ext.jwhthtmltopojo.intrf.HtmlField;
import fr.whimtrip.ext.jwhtscrapper.annotation.Link;
import fr.whimtrip.ext.jwhtscrapper.annotation.LinkObject;
import fr.whimtrip.ext.jwhtscrapper.annotation.LinkObjects;
import fr.whimtrip.ext.jwhtscrapper.annotation.WarningSign;
import fr.whimtrip.ext.jwhtscrapper.exception.WarningSignException;
import org.jsoup.nodes.Element;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScrapperHtmlAdapter<T> extends DefaultHtmlAdapterImpl<T> {


    public ScrapperHtmlAdapter(HtmlToPojoEngine htmlToPojoEngine, Class<T> clazz) {
        super(htmlToPojoEngine, clazz);
    }

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
                checkWarningSign(htmlField.getField(), fieldValue);

                htmlField.setFieldOrThrow(newInstance, fieldValue);
            }
        }
        return newInstance;

    }

    public Field getLinkObject(HtmlToPojoAnnotationMap<Link> link)
    {

        List<? extends HtmlToPojoAnnotationMap<LinkObject>> linkObjectFields = getFieldList(LinkObject.class);
        for(HtmlToPojoAnnotationMap<LinkObject> linkObject : linkObjectFields)
        {
            if(linkObject.getAnnotation().value().equals(link.getName()))
                return linkObject.getField();
        }



        List<? extends HtmlToPojoAnnotationMap<LinkObjects>> linkObjectsFields = getFieldList(LinkObjects.class);
        for(HtmlToPojoAnnotationMap<LinkObjects> linkObjects : linkObjectsFields)
        {
            if(Arrays.asList(linkObjects.getAnnotation().value()).contains(link.getName()))
                return linkObjects.getField();
        }

        return null;
    }


    private void checkWarningSign(Field field, Object value) throws WarningSignException {
        if(field.getAnnotation(WarningSign.class) != null)
        {
            if( isWarningSignTriggered(field, value))
                throw new WarningSignException(field);
        }
    }

    private boolean isWarningSignTriggered(Field field, Object value) {
        boolean retry = false;

        WarningSign warningSign = field.getAnnotation(WarningSign.class);

        if(        warningSign.triggeredOn() != WarningSign.TriggeredOn.ANY_VALUE_MATCHING_REGEX
                && warningSign.triggeredOn() != WarningSign.TriggeredOn.ANY_VALUE_NOT_MATCHING_REGEX)
        {

            Selector selector = field.getAnnotation(Selector.class);
            Object castedDefaultValue = null;
            try {
                castedDefaultValue = HtmlToPojoUtils.castValue(
                        selector.defValue(),
                        value.getClass(),
                        "",
                        selector.locale().equals(Selector.NO_VALUE) ?
                                Locale.getDefault() : Locale.forLanguageTag(selector.locale())
                );
            }catch(IllegalArgumentException e)
            {
                throw new ParseException(selector.defValue(), Locale.getDefault(), field);
            }

            boolean isNullOrEquivalent =
                    (value == null || (value instanceof String && ((String) value).isEmpty() )
                            || (value instanceof List && ((List) value).isEmpty()));
            boolean isEqualAsNumbers =
                    (value instanceof Number && WhimtripUtils
                            .compareNumbers(
                                    (Number) value,
                                    (Number) castedDefaultValue
                            ) == 0
                    );
            boolean isEqualToDefaultValue = ( isEqualAsNumbers || value == castedDefaultValue || value.equals(castedDefaultValue));

            if(
                    (warningSign.triggeredOn() == WarningSign.TriggeredOn.NULL_VALUE && isNullOrEquivalent)
                            || (warningSign.triggeredOn() == WarningSign.TriggeredOn.DEFAULT_VALUE && isEqualToDefaultValue)
                            || (warningSign.triggeredOn() == WarningSign.TriggeredOn.ANY_CORRECT_VALUE && !isNullOrEquivalent && !isEqualToDefaultValue)
                    )
            {
                retry = true;
            }
        }

        else if(value instanceof String)
        {
            Pattern pattern = Pattern.compile(warningSign.triggeredOnRegex());
            Matcher m = pattern.matcher((String)value);
            boolean match = m.find();
            if(warningSign.triggeredOn() == WarningSign.TriggeredOn.ANY_VALUE_NOT_MATCHING_REGEX && !match
                    || warningSign.triggeredOn() == WarningSign.TriggeredOn.ANY_VALUE_MATCHING_REGEX && match)
            {
                retry = true;
            }
        }

        return retry;
    }

}
