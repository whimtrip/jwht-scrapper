/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.intfr;

import fr.whimtrip.ext.jwhtscrapper.service.scoped.LinkPreparatorHolder;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by LOUISSTEIMBERG on 19/11/2017.
 */
public interface LinkListFactory<P> {

    List<LinkPreparatorHolder> createLinkPreparatorLists(P parent, Field parentField);

}
