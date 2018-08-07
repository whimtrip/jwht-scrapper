/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.intfr;

import fr.whimtrip.ext.jwhtscrapper.annotation.LinkListsFromBuilder;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkListScrappingContext;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkPreparatorHolder;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

/**
 *
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 30/07/18</p>
 *
 * <p>
 *     This interface can be used with {@link LinkListsFromBuilder#value()}.
 *     This defines the processing unit that will transform a given parent
 *     class and the associated field to create the list for into a list of
 *     {@link LinkPreparatorHolder}. This method will be called through {@link LinksFollower}
 *     and will outut {@link LinkListScrappingContext} that will be used as is
 *     to scrap the child corresponding POJO.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public interface LinkListFactory<P> {

    /**
     * <p>
     *     This defines the processing unit that will transform a given parent
     *     class and the associated field to create the list for into a list of
     *     {@link LinkPreparatorHolder}. This method will be called through {@link LinksFollower}
     *     and will outut {@link LinkListScrappingContext} that will be used as is
     *     to scrap the child corresponding POJO.
     * </p>
     * @param parent the parent POJO instance on which the value of the resulting
     *               list of POJOs generated from the returned {@link LinkPreparatorHolder}
     *               list will be set to.
     * @param parentField the field belonging to the parent class whose resulting
     *                    POJO list scrapped from the returned {@link LinkPreparatorHolder}
     *                    list will be set to.
     * @return a list of {@link LinkPreparatorHolder} list defining how the scrapping
     *         operation should be performed to populate the required list of POJOs from
     *         parent field.
     */
    List<LinkPreparatorHolder> createLinkPreparatorLists(@NotNull final P parent, @NotNull final Field parentField);

}
