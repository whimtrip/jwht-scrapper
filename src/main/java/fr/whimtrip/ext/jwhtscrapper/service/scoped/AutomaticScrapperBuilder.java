/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.core.util.WhimtripUtils;
import fr.whimtrip.ext.jwhtscrapper.annotation.AutomaticScrapping;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by LOUISSTEIMBERG on 21/11/2017.
 */

public class AutomaticScrapperBuilder<T>  {

    private Class<T> modelType;

    private List<FieldToBeScrapped> fieldsToBeScrapped = new ArrayList<>();

    public AutomaticScrapperBuilder(Class<T> parentObjectClazz)
    {
        modelType = parentObjectClazz;
        for(Field fld : modelType.getDeclaredFields())
        {
            AutomaticScrapping scrapping = fld.getAnnotation(AutomaticScrapping.class);

            if(scrapping != null)
            {
                fieldsToBeScrapped.add(
                        new FieldToBeScrapped()
                                .setAutomaticScrapping(scrapping)
                                .setField(fld)
                );
            }
        }
    }

    public AutomaticScrapperBuilder() {

    }

    public List<ScrappingContext> prepareScrappers(Object parentObject, String... fields) throws IllegalAccessException
    {
        selectFields(fields);
        List<ScrappingContext> contexts = new ArrayList<>();

        for (FieldToBeScrapped fieldToBeScrapped : fieldsToBeScrapped) {

            Class<? extends ScrapperHelper> helperClazz =
                    fieldToBeScrapped.getAutomaticScrapping().value();

            ScrappingContext context = instanciateContext(
                    WhimtripUtils.getObjectFromField(
                            fieldToBeScrapped.field,
                            parentObject
                    ),
                    helperClazz,
                    fieldToBeScrapped.getField().getName()
            );

            contexts.add(context);
        }
        return contexts;
    }




    /**
     * Selecting wich of the fields annotated with @AutomaticScrapping annotation
     * needs to be used by our scrapper
     * @param fields
     */
    private void selectFields(String... fields) {
        if(fields != null && fields.length != 0)
        {

            List<String> fieldsList = Arrays.asList(fields);

            List<FieldToBeScrapped> copiedFieldsToBeScrapped = new ArrayList<>();
            copiedFieldsToBeScrapped.addAll(fieldsToBeScrapped);

            for(FieldToBeScrapped fld : copiedFieldsToBeScrapped)
            {
                if(!fieldsList.contains(fld.getField().getName()))
                    copiedFieldsToBeScrapped.remove(fld);
            }

            fieldsToBeScrapped = copiedFieldsToBeScrapped;
        }
    }

    @SuppressWarnings("unchecked")
    private <C, P, M,  H extends ScrapperHelper<P, M>> ScrappingContext<C, P, M, H> instanciateContext(
            C containerObject,
            Class<H> helperClazz,
            String fieldName
    )
    {
        H helper = WhimtripUtils.createNewInstance(helperClazz);

        ScrappingContext<C, P, M, H> context = new ScrappingContext<>();
        context.helper = helper;
        context.containerObject = containerObject;
        if(containerObject instanceof List)
        {
            if(((List<P>) containerObject).isEmpty())
                context.parentClazz = (Class<P>) Object.class;
            else
                context.parentClazz = (Class<P>) ((List<P>) containerObject).get(0).getClass();
        }
        context.name = fieldName;
        context.requestScrappingContext = helper.init();
        context.modelClazz = context.requestScrappingContext.getModelClass();
        return context;
    }

    public <P> List<ScrappingContext> prepareScrappers(List<P> parentObjs, Class<? extends ScrapperHelper<P, ?>> helperClazz) {

        return List.of(
                instanciateContext(
                    parentObjs,
                    helperClazz,
                    parentObjs.get(0).getClass().getSimpleName()
                )
        );
    }


    public static class FieldToBeScrapped
    {
        Field field;
        AutomaticScrapping automaticScrapping;

        FieldToBeScrapped setField(Field field) {
            this.field = field;
            return this;
        }

        FieldToBeScrapped setAutomaticScrapping(AutomaticScrapping automaticScrapping) {
            this.automaticScrapping = automaticScrapping;
            return this;
        }

        public Field getField() {
            return field;
        }

        public AutomaticScrapping getAutomaticScrapping() {
            return automaticScrapping;
        }
    }
}
