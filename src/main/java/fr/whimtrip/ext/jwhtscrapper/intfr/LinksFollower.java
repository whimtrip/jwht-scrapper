package fr.whimtrip.ext.jwhtscrapper.intfr;

import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhthtmltopojo.intrf.HtmlAdapter;
import fr.whimtrip.ext.jwhtscrapper.annotation.HasLink;
import fr.whimtrip.ext.jwhtscrapper.annotation.Link;
import fr.whimtrip.ext.jwhtscrapper.annotation.LinkListsFromBuilder;
import fr.whimtrip.ext.jwhtscrapper.annotation.LinkObject;
import fr.whimtrip.ext.jwhtscrapper.annotation.LinkObjects;
import fr.whimtrip.ext.jwhtscrapper.exception.LinkException;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkListScrappingContext;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkScrappingContext;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.HtmlAutoScrapperImpl;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 30/07/18</p>
 *
 * <p>
 *     This interface defines the basic contract any {@link LinksFollower} class should accept.
 *     A {@link LinksFollower} is basically the processing unit that will find any links to further
 *     scrap on your POJOs, follow them, and populate new child POJOs with those links to
 *     further complete your scrapping scheme. Under the hood, it should be using the
 *     {@link HtmlAdapter} of the parent POJO that itself will require to use the
 *     HtmlAdapter of its child POJOs.
 * </p>
 *
 * <p>
 *     The LinksResolver can neither scrap himself the links to follow nor can it populate
 *     the parent POJOs' fields with the resulting values because this processing falls
 *     under the responsability of the {@link HtmlAutoScrapper}, and especially because
 *     the {@link HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)} was especially
 *     designed to this extent. In fact, rather than this, {@link HtmlAutoScrapper} uses
 *     a {@link LinksFollower} but the opposite is not true to avoid circular dependency
 *     patterns.
 * </p>
 *
 * <p>
 *     That's why the {@link #resolveBasicLinks()} method should only search for all the
 *     links to scrap on a scrapped POJO an its child POJOs. If any links to be scrapped
 *     are found, it should prepare the corresponding HTTP requests {@link BoundRequestBuilder},
 *     and assemble all of this in containers objects that will hold all the necessary
 *     context to call the scrapping method from the {@link HtmlAutoScrapper} and assign
 *     the resulting value to the correct field of the correct parent. This is what this
 *     class must do : scanning links to follow, preparing them and put the ready to be
 *     scrapped links and corresponding information in ready to use containers {@link LinkScrappingContext}
 *     and {@link LinkListScrappingContext}.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public interface LinksFollower {


    /**
     * <p>
     *     This method should only search for all the links to scrap on a scrapped POJO an
     *     its child POJOs. If any links to be scrapped are found, it should prepare the
     *     corresponding HTTP requests {@link BoundRequestBuilder}, and assemble all of this
     *     in containers objects that will hold all the necessary context to call the
     *     scrapping method from the {@link HtmlAutoScrapper} and assign the resulting value
     *     to the correct field of the correct parent. This is what this class must do :
     *     scanning links to follow, preparing them and put the ready to be scrapped links
     *     and corresponding information in ready to use containers {@link LinkScrappingContext}
     *     and {@link LinkListScrappingContext}.
     * </p>
     *
     *
     * <p>
     *     Scanning links implies some requirements :
     * </p>
     * <ul>
     *     <li>
     *         You should only search for links in the parent POJO. If one Child POJO
     *         field in this parent POJO is annotated with annotatio {@link HasLink},
     *         you should recursively analyse this sub POJO for links. If it happens
     *         to be a List of POJO, you should find and resolve links for each and
     *         everyone of its sub POJO elements.
     *     </li>
     *     <li>
     *         You should then search for {@link Link} annotations on {@link String}
     *         fields. Such annotated field must come with a dedicated {@link LinkObject}
     *         or {@link LinkObjects} which will be a custom POJO typed field on which
     *         you will set the resulting scrapped POJO instance from the submitted
     *         {@link Link}. If this is an {@link LinkObjects}, then it will be a list
     *         of POJOs completed by several {@link Link} annotations from several
     *         String typed fields of the POJO linking to the same {@link LinkObjects}.
     *     </li>
     *     <li>
     *         You should finally search for {@link LinkListsFromBuilder} on a field which
     *         will define List of Link to further scrap that should directly be set to
     *         the List typed field annotated with this annotation.
     *     </li>
     * </ul>
     *
     * <p>
     *     Please also note that any implementing class should respect all of the parameters
     *     submitted within the corresponding annotations {@link Link}, and {@link LinkListsFromBuilder}.
     * </p>
     *
     * @throws LinkException when one link could not be properly resolved and prepared.
     *                       This will only happend if {@code throwExceptions} was
     *                       enabled in any underlying {@link LinkScrappingContext}.
     *                       Otherwise, the exception will be logged within the furnished
     *                       {@link ExceptionLogger}.
     */
    @SuppressWarnings("unchecked")
    void resolveBasicLinks() throws LinkException;

    /**
     * @return the current list of {@link LinkScrappingContext} to be followed and computed
     *         by the {@link HtmlAutoScrapper}. If method {@link #resolveBasicLinks()} hasn't
     *         yet been called, it should return an empty list.
     */
    @NotNull
    List<LinkScrappingContext> getScrappingContexts();

    /**
     * @return the current list of {@link LinkListScrappingContext} to be followed and computed
     *         by the {@link HtmlAutoScrapper}. If method {@link #resolveBasicLinks()} hasn't
     *         yet been called, it should return an empty list. Link lists are list of scraps
     *         to be performed and mapped to a sing POJO type that will be set to a single
     *         list typed field.
     */
    @NotNull
    List<LinkListScrappingContext> getLinkListsScrappingContexts();
}
