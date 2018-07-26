package fr.whimtrip.ext.jwhtscrapper.service.base;

import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     This interface provide a description of how an AutomaticScrapperManager
 *     should behave.
 * </p>
 *
 * <p>
 *     This manager class is a factory pattern class that will instanciate
 *     ready to use {@link AutomaticScrapperClient} with only the list of objects
 *     to initiate the scrapping from and the helper class to coordinate
 *     them all.
 * </p>
 *
 * <p>
 *     This is meant to be used and shared at the application level altough several
 *     different AutomaticScrapperManager can coexist at the application level, it
 *     is usually a common use case to use only one in the application scope.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public interface AutomaticScrapperManager {

    /**
     *
     * <p>
     *     This method is a simple factory method to instanciate and
     *     prepare a {@link AutomaticScrapperClient}.
     * </p>
     *
     * @param parentObjs the objects that will be used to create the urls
     *                   to scrap and be reused all along the scrapping
     *                   process through the correct {@link ScrapperHelper}.
     * @param helper the helper class that will guide and direct the whole
     *               scrapping process as well as its configurations.
     * @param <P> the type of Parent Objects
     * @param <H> the type of Helper Clazz
     * @return a {@link AutomaticScrapperClient} built out of the submited input.
     */
    <P, H extends ScrapperHelper<P, ?>> AutomaticScrapperClient createClient(
            @NotNull final List<P> parentObjs,
            @NotNull final H helper
    );
}
