# jwht-scrapper - Fully Featured Java Scrapping highly pluggable and customizable framework 

# Introduction
This lib provides a fully featured, highly pluggable 
and customizable Java Scrapping framework. It was mainly designed 
to scrap web pages as HTML but other mapping types are supported
thanks to custom user-defined Object mapper implementation possibilities.

It was built with our HTML to POJO scrapping framework 
[jwht-htmltopojo](https://github.com/whimtrip/jwht-htmltopojo)
which provides a fully featured framework to easily map HTML 
pages to very complex POJOs using simple annotations.

jwht-scrapper features a complete javadoc that can be seen from 
github/official code sources to fully interact with all the 
possibilities offered by this library. Heavily interfaced, most 
processing units can be replaced or extended to fit your use case
if required.

This lib uses under the hood [asynchttpclient](https://github.com/AsyncHttpClient/async-http-client) 
as its async http client because of its very flexible and complete API. We also
use [jackson java lib](https://github.com/FasterXML/jackson-docs) to provide a
default JSON to POJO alternate gateway instead of our standard HTML to POJO 
gateway.


# Main Features


This library was originally built to support real world scrapping 
use cases. Most features were implemented because we felt a need 
for them to be part of this framework.

- [Proxy support](#proxy-finder)
- [Warning Sign Support to detect captchas and more](#warning-signs)
- [Complex link following features](#links)
- [Multithreading](#parameter-parallelthreads-)
- [Various scrapping delays when required](#delay-and-waits-parameters)
- [Rotating User-Agent](#parameter-rotatinguseragent-)
- [Request auto retry and HTTP redirections supports](#parameter-maxrequestretries-) 
- [HTTP headers, cookies and more support](#http-parameters)
- [GET and POST support](#parameter-method-)
- [Annotation Configuration](#scrapper-helper-annotations)
- [Detailed Scrapping Metrics](#get-metrics)
- [Async handling of the scrapper client](#scraper-client)
- [jwht-htmltopojo fully featured framework to map HTML to POJO](https://github.com/whimtrip/jwht-htmltopojo)
- [Custom Input Format handling and built in JSON -> POJO mapping](#custom-object-mapper)
- [Full Exception Handling Control](#exception-logger)
- [Detailed Logging with log4j](#logging)
- [POJO injection](https://github.com/whimtrip/jwht-htmltopojo#injection)
- Custom processing hooks
- Easy to use and well documented API



## Quick Start :

### Maven Installation

```xml

<dependency>
    <groupId>fr.whimtrip</groupId>
    <artifactId>whimtrip-ext-scrapper</artifactId>
    <version>1.0.0</version>
</dependency>

```

### Build your HTML POJO

You will in most cases scrap Web Pages (we will talk later about
API or other sources scrapping). You need a properly annotated
POJO class to map the HTML pages to. To do so, follow the instructions
[here](https://www.github.com/whimtrip/jwht-htmltopojo#quick-start). 
You can ignore the part about starting the engine as it will be handled
under the hood by this library.

### Build Your Scrapper Helper

One last thing you need is your Scrapper Helper implementation. This
class will orchestrate the whole scrapping process and will also hold
a compulsory`@Scrapper` annotation to define your [configurations](#configurations).

```java

@Scrapper(
        // Perform GET request
        method = Link.Method.GET,
        // Map the resulting pages on this class
        scrapModel = Restaurant.class,
        // Scrap no more than 100 pages
        scrapLimit = 100,
        requestsConfig =
            @RequestsConfig(
                    // de
                    defaultCookies = {
                            @Cookie(
                                    name = "SetCurrency",
                                    value = "EUR",
                                    domain = ".cool-restaurants.com"
                            )
                    },
                    defaultHeaders = {
                            @Header(
                                    name = "Host",
                                    value = "www.cool-restaurants.com"
                            )
                    },
                    // After 12 seconds, the requests will be timed out. 
                    timeout = 12_000, 
                    // The max number of retries per single request
                    maxRequestRetries = 110,
                    // The max number of parrallel running scraps 
                    parallelThreads = 40, 
                    // wait 100ms between each single requests. Thread safe : your 40 threads
                    // will wait for that delay and will be synchronized on the same request
                    // handler for the whole scrapper client.
                    waitBetweenRequests = 100, 
                    // If a captcha was detected, wait 300seconds
                    warningSignDelay = 300_000,
                     // Follow HTTP 301/302 redirections (only once) 
                    followRedirections = true,
                    // Custom proxy config to make usage of proxies for this scrapping
                    proxyConfig = @ProxyConfig(useProxy = true)
            )
)
public class CoolRestaurantsScrapperHelper implements ScrapperHelper<UncompletedRestaurant, Restaurant>{

    // If you're using both Spring + Hibernate, this might be useful.
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Override
    public boolean shouldBeScrapped(@NotNull UncompletedRestaurant parent) {
        // Scrap it only if the parent object was never scrapped before for example.
        return !parent.hasAllInformations();
    }

    @Override
    public String createUrl(@NotNull UncompletedRestaurant parent) {
        return "https://www.cool-restaurants.com/" + parent.getId() + "?showMeals=true";
    }

    @Override
    public void editRequest(@NotNull BoundRequestBuilder req, @NotNull UncompletedRestaurant parent, @NotNull BoundRequestBuilderProcessor processor)
    {
        // Just an example of what you can do
        if(parent.isPopular())
            processor.addHeader("Some-Header-That-Does-Some-Magic", "Some-Secret-Value", req);
        return req;
    }

    @Override
    public Restaurant instanciateModel(@NotNull UncompletedRestaurant parent) {
        // If you need to perform some code injection with the parent object, you can do that here.
        return new Restaurant(parent); 
    }


    @Override
    public void buildModel(@NotNull UncompletedRestaurant parent, @NotNull Restaurant model)
    {
        // Let's pretend we don't want to make anything with it here.
        // If you want to modify your parent object or POJO instance 
        // at the end of the scrapping, you should do this here!
    }

    @Override
    public boolean shouldBeSaved(@NotNull UncompletedRestaurant parent, @NotNull Restaurant model) {
        // We might for example check that all necessary informations are present 
        // on our POJO before saving it. Saving it will call save method just below.
        return parent.hasAllInformations();
    }
    
    @Override
    public void save(@NotNull final P parentObject, final M model) {
        // just an example of course
        restaurantRepository.save(model);
    }

    @Override
    public Object returnResult(@NotNull Accommodation parent, @NotNull TaSearchListing model) {
        // Returns something or a null value out of the scrap. This will then be retrieved
        // in the output return List when gathering the scrapping results.
        return parent;
    }

    @Override
    public boolean wasScrapped(@NotNull Accommodation parent, @NotNull TaSearchListing model) {
        // This will only be used for metrics purposes so that this can be accounted as a
        // valid scrap or a fail. You can return true every time if you do not want to use 
        // this feature
        return model.wasScrapSucessful();
    }
}


``` 

### Let's Scrap!

Now we just need to create a client and start the scrap. One client
instance cannot be reused. The Scrapper manager should in most cases
be an application scope global var you could retrieve with for example
`@Autowired` annotation if you use Spring framework.

```java

List<UncompletedRestaurants> uncompletedRestaurantsToScrap = uncompletedRestaurantsRepository.findAll();

AutomaticScrapperManager scrapperManager = 
        new AutomaticScrapperManagerBuilder()
              
            // if you're scrapping an API instead of HTML pages, 
            // you could use the following config :
            //.setJsonScrapper(true)
            .build();

AutomaticScrapperClient scrapperClient = 
        scrapperManager.createClient(
            uncompletedRestaurants,
            new CoolRestaurantsScrapperHelper()
        );

// Start the scrapping in a separate thread
scrapperClient.scrap();


// Now just wait for your scrap to end!
// You can also terminate the scrapper, get its results with or without a timeout, get some
// statistics about the scrapping...

```


# Full Documentation

## Configurations

This library features some basic configurations you can play with easily
using the default API of this framework. We'll try to showcase them here.

### Scrapper Helper Class

The Scrapper Helper class usage is showcased [here](#build-your-scrapper-helper)
and detailed javadoc can be found [here](https://github.com/whimtrip/jwht-scrapper/blob/master/src/main/java/fr/whimtrip/ext/jwhtscrapper/intfr/ScrapperHelper.java).

### Scrapper Helper Annotations

Annotation configuration works with three main annotations :

#### @Scrapper

This is the annotation that **must appear** on top of your ScrapperHelper
implementation. It will contain all other configuration annotations.

###### Parameter `scrapModel` :

The POJO on which the Raw HTTP Body will be mapped. In our above example :
 `Restaurant.class`

###### Parameter `method` :

The HTTP method to use. Currently, only `GET` and `POST` method are supported.
Default value is `GET`.

###### Parameter `throwExceptions` :

`false` if scrapping exceptions should be ignored and `true` if they should be 
thrown to stop the current item scrap (not the whole scrapper client). Default
value is true.

###### Parameter `scrapLimit` :

The scrapping limit which defines the maximum number of starting pages to scrap.
This can prove to be very useful when testing your scrapper configurations and
POJOs. Default value is 100.

###### Parameter `requestsConfig` :

Defines here your HTTP configuration. You must provide an `@RequestsConfig` 
annotation as the value of this parameter. See below for a description of its
parameters.

#### @RequestsConfig


##### General Purpose Parameters
###### Parameter `parallelThreads` :

The max number of parrallel threads to run at the same time. You should start
by testing it with quite low values and progressively increase it if no ban 
or errors comes out of your first tests.


###### Parameter `maxRequestRetries` :

The maximum number of times a single request can be retried before a
RequestMaxRetriesReachedException will be thrown. If using proxies, it is 
strongly advised to set this parameter to 20-100 as most commonly used 
proxies tends to work once every 10-20 times and once this exception is 
thrown, the current scrap cannot be retried.

##### Delay and Waits Parameters
###### Parameter `waitBetweenRequests` :

The minimum delay in milliseconds that must be waited between two requests. 
This parameter will be taken into account even though requests are made
asynchronously and from several threads at a time.

###### Parameter `timeout` : 

The request timeout in milliseconds. If you use proxies or if you poll slow 
websites/webpages, it is recommended to set it quite high altough you should
test many setups, especially with proxies to find the best compromise between
performances and request efficiency.


###### Parameter `periodicDelay` :

The delay in milliseconds on successful thread removal. This means that each
time the scrapper client gather properly finished threads, it will pause for
the given delay. 0 means it won't wait.

###### Parameter `warningSignDelay` :

The delay to wait for when a `@WarningSign` is triggered. It will only work if 
`WarningSign.pausingBehavior()` set to `PausingBehavior.PAUSE_ALL_THREADS` or
`PausingBehavior.PAUSE_CURRENT_THREAD_ONLY`. See [here](#warning-signs) for 
more information.


##### HTTP Parameters
###### Parameter `defaultCookies` : 

The default cookies to use on each requests if any. Very useful for selecting
a default currency and language that does not depends on the current proxy 
used which most of the times ends up quite poorly with regex for example.
Most websites uses cookies for preferences.

###### Parameter `defaultHeaders` :

The default headers to use on each request. Usually helpful for `Host` header
for example.

###### Parameter `defaultPostFields` :

The default POST fields to use on POST requests only.

###### Parameter `followRedirections` : 

A boolean defining wether HTTP 301 and 302 redirections should or shouldn't 
be followed.

###### Parameter `allowInfiniteRedirections` :

A boolean defining wether redirections should or shouldn't be followed indefinitely.
Sometimes, HTTP infinite redirections loops can happen and that's why the default 
value of this parameter is `false`. Will only be used if `followRedirections` is 
set to true. Otherwise, it won't have any effect.


##### Special Features :

###### Parameter `rotatingUserAgent` :

A boolean indicating wether rotating user agent should be used or not. If set to true,
the header `User-Agent` header will rotate at each request. Using rotating user agent
will discard any `User-Agent` header set as default using `defaultHeaders`.

###### Parameter `proxyConfig` :

Defines here your Proxy configuration. You must provide an `@ProxyConfig` 
annotation as the value of this parameter. See below for a description of its
parameters.

#### @ProxyConfig

###### Parameter `useProxy` :

A boolean indicating if proxies should be used or not. If it returns `true`, you 
**must** provide your custom [ProxyFinder](#proxy-finder) implementation using
`AutomaticScrapperManagerBuilder.setProxyFinder(proxyFinder)` method  when creating
your `AutomaticScrapperManager`.

###### Parameter `connectToProxyBeforeRequest` :

Wether TCP Connect should be used before making the actual HTTP request. This feature
will be deprecated in a near future because it proved to be useless.

###### Parameter `proxyChangeRate` :

The number of requests to perform before changing the Proxy. Currently not supported 
because proxies change at almost every request try as they tend to be very unstable. 
Currrently deprecated.

### Scrapper Client Configurations

Your scrapper client configuration can also be tuned and customised altough it is
a much more complex task. To do so, you need to input below described implementations
through the `AutomaticScrapperManagerBuilder` class. 

Here is a short example on how to register your custom implementations :

```java
AutomaticScrapperManager scrapperManager = 
        new AutomaticScrapperManagerBuilder()
             //.setExceptionLogger(...)
             //.setCustomObjectMapper(...)
             //...
            .build();
```

#### Exception Logger

You can submit your own `ExceptionLogger` implementation here. This will provide
a way to use a custom processing for exception handling. For our own implementation,
we save the exceptions in the database, trigger an alarm so that the uncaught 
exception can be corrected as soon as possible, and finally log the stacktrace. This
is just an example of what you can do with such exception logger service.

#### HtmlToPojoEngine

You can provide your own `HtmlToPojoEngine` implementation altough it is really
one of the hardest and most dangerous customisation operation as it might clash 
with current implementations contracts.

If you are just providing your own `HtmlToPojoEngine` instance using the standard 
API, then this won't cause any problem and could be useful to reuse previously analysed
POJOs.

`HtmlToPojoEngine` has a built in cache so it might be useful to reuse the same
instance throughout the application scope. To instanciate a correct `HtmlToPojoEngine`
compatible with all of this API feature, you should use the following piece of 
code :

```java
HtmlToPojoEngine.create(new ScrapperHtmlAdapterFactory());
```

#### Custom Object Mapper

If you do not plan to receive HTML body responses from the scraps to perform, then 
you can use a custom object mapper implementation using for example
`public MyObMapper implements BasicObjectMapper` to map other input formats to POJOs
(Link annotations are still supported but not warning signs so you can annotate your
POJOs with [links annotations](#links)).

If you do :

```java

        new AutomaticScrapperManagerBuilder()
            .setJsonScraper(true)
            // ...
            .build();
```

Then a Jackson object mapper will be used to turn JSON strings to proper Jackson 
annotated POJOs.

#### Custom Async Http Client

If you want to set special default properties for your `AsyncHttpClient`, you
can submit your own instance.

#### Proxy Finder

If you want to use proxies, you have to set here your own proxy finder implementation.
This is a class instance that can retrieve proxies from a given source (usually a database)
modify their status to mark them as frozen or banned proxies for example, and finally
persist them to the database when necessary. When implementing `ProxyFinder` with 
your own proxy finder class, you'll be able to discover our javadoc for both `ProxyFinder`
class and `Proxy` interface.

#### JSON scrapper

If set to `true`, it will instanciate a default `BasicObjectMapper` as explained 
[above](#custom-object-mapper) so that a jackson default `ObjectMapper` wrapper
will be used to implement `BasicObjectMapper`, therefore being able to convert
JSON strings to POJOs.


#### Request Processor

Because we use a request builder and because most builder patterns in java does
not feature getters, some basic tasks to modify the request were made impossible
(such as adding an header as it require to retrieve the `HttpHeaders` instance
first.). Default implementation provided allows this. Please [see more](https://github.com/whimtrip/jwht-scrapper/blob/master/src/main/java/fr/whimtrip/ext/jwhtscrapper/service/base/BoundRequestBuilderProcessor.java)
to watch a request processor contract. You can provide your own given those
javadoc instructions but it not very recommended for basic configurations needs.

## Links

### General Knowledge

In many cases, scrapping will start with a search query providing several results, 
once you have those results, you want to match the correct result given the 
information you already have on your initial search query. Once you have the correct
result you would probably want to go to the result detailed page. And maybe from
this page you'll want to scrap another one, and another one... 

This is why Link support was built for in this framework. The idea is that it can 
handle a complete scrap whatever steps are involved into it with one single 
`ScrapperHelper` implementation and all your POJOs properly annotated.

The basic idea is that you annotate a String typed field with an `@Link`,
this field will contain the url to "click/follow", and the field to assign
the result to  with an `@LinkObject`. 

From a coding perspective, this might look like that :

```java

public class MyPojoExample {
    
    
    @Selector(
            value = "some-css-attribute",
            attr = "href"
            // eventually some more stuff here for the 
            // url to be gathered properly
    )
    @Link
    private String someUrlInAnHref;
    
    @LinkObject(
            // name of the url field it is linked to
            value = "someUrlInAnHref"
    )
    private MyChildPojoExample childPojoExample;
    
    
    // getters and setters
}

```


There is a little more theory and we'll try to explain it below.

### @Link

The very first annotation we need to explain here is `@Link`.
As stated earlier, this must appear on a String typed field.

There are some parameters you could tune for your link such as :


- `method` : The HTTP method to use.
- `requestEditor` : specify an [HttpRequestEditor](#httprequesteditor)
custom implementation that will process and check your request before 
it will be sent to the network.
- `editRequest` : wether the request should be edited or not and
if set to true, a custom [HttpRequestEditor](#httprequesteditor)
implementation has to be provided as well so that the request 
can be edited.
- `regexCondition` : specify a regex condition to 
validate the link before following it.
- `fields` : add `POST` fields to your request. Will only work
for `POST` requests.
- `followRedirections` : tune the [HTTP redirection behavior](#parameter-followredirections-)
of this link.
- `throwExceptions` : tune the [exception handling behavior](#parameter-throwexceptions-)
of this link.

Once you've parametizered your `@Link` annotation on your String typed
field, there is one more step : where will this link scrapped result 
will be added to and to what POJO will it be mapped to ? This will 
be specified by one of the two below mentionned annotations : `@LinkObject`
and `@LinkObjects`.


#### @LinkObject

In the same POJO class as the one with your `@Link` annotation, you can 
provide an `@LinkObject` annotation on top of a child POJO typed field.
The `value` param of this annotation must be set to the `@Link` annotated
field name so that the processing units can understand which `@Link` must
be assigned to which `@LinkObject`.

#### @LinkObjects

Alternatively, you can provide an `@LinkObjects` instead of an `@LinkObject`
annotation. This annotation can only be set on a list of child POJO typed
field and will receive a list of all scrapped results from several `@Link`
annotated fields of this class, all of which will be mapped to the same
child POJO which will be the one specified in the `List<ChildPojo>` of
your `@LinkObjects` annotated field. The `value` parameter of this 
annotation should contain all the `@Link` annotated fields names you
want to put into this list.

We put together some code example to make it clearer :

```java
public class MyParentPojoClass {
    
    /* Our four different links */
    
    @Selector(/*Some stuffs in here to select the url*/)
    @Link
    private String firstLink;
    
    @Selector(/*Some stuffs in here to select the url*/)
    @Link
    private String secondLink;
    
    @Selector(/*Some stuffs in here to select the url*/)
    @Link
    private String thirdLink;
    
    @Selector(/*Some stuffs in here to select the url*/)
    @Link
    private String fourthLink;
    
    // This one will only contain the third link value
    // mapped as MyFurstChildPOJO
    @LinkObject(value = "thirdLink")
    private MyFirstChildPOJO thirdLinkValue;
    
    
    // This one will contain first, second and fourth values
    // mapped as MySeconChildPOJO instances and put together
    // in a List.
    @LinkObjects(
            value = {
                    "firstLink",
                    "secondLink",
                    "fourthLink"
            }
    )
    private List<MySecondChildPOJO> otherLinksValues;
    
    // getters and setters
    
}
```

#### HttpRequestEditor

The `HttpRequestEditor` interface can be implemented and provided
in `@Link` annotations for example (there are some other use cases
we will talk about later on) to edit the request and control its
behavior with more fine grained tools.

Implementing the `HttpRequestEditor` interface is as simple as :

```java

public class HttpRequestEditorExample implements HttpRequestEditor<ParentPojoClass, ChildrenPojoClass> {
    
    @Override
    public void init(Field field) {
        // If you want to pick up annotations on the field annotated with `@LinkObject` or
        // `@LinkObjects` to create a generic HttpRequestEditor for example.
    }

    @Override
    public boolean shouldDoRequest(ParentPojoClass parentContainer) {
        // decide wether the request should be performed or not
        return parentContainer.isLinkScrapWorthIt();
    }

    @Override
    public void prepareObject(ChildrenPojoClass obj, ParentPojoClass parentContainer, LinkPreparatorHolder linkPreparatorHolder) {
        // Called with the newly instanciated object in order to prepare this object
        // If you want to perform some code injections you can do this here altough 
        // in most cases, this method will be useless.
    }

    @Override
    public void editRequest(BoundRequestBuilder req, LinkPreparatorHolder preparatorHolder, BoundRequestBuilderProcessor requestProcessor) {
        // You can modify your HTTP request here. for example by adding some more headers : 
        requestProcessor.addHeader(
                "Some-Header", 
                preparatorHolder.getParent().getCustomHeaderValue(), 
                req
        );
    }

```

### @LinkListsFromBuilder

This one is a little bit more trickier but allows to perform some really interesting 
tasks. The idea is that you will put this on top of a List of Child POJOs typed field 
like this one :

```java
@LinkListsFromBuilder(/* Some stuff in here */)
private List<MySecondChildPOJO> otherLinksValues;
```

Now you must provide a `LinkListFactory` implementation in the `@LinkListsFromBuilder`
annotation.

Such factory will need to return a list of `LinkPreparatorHolder` which is just a standard
POJO containing basically the same informations as the [`@Link` annotation](#link).
All features available in `@Link` are made available through the constructor of your
`LinkPreparatorHolder` except one : `editRequest` which can only be modified from
the `@LinkListsFromBuilder` annotation itself.

### @HasLink

In complex scrapping situations, which is often the case when you need to use links, 
you have nested POJOs everywhere. Link scanning does, by default only happen in parent
POJO level following the last scrap to preserve CPU and memory consumption. If your link
is nested in a child POJO, you'll have to annotate the parent POJO field containing your
nested POJO with an `@HasLink` annotation.

Let's give some code example of what will and won't work :

```java
public class MyParentPOJO {
    
    // required for my child POJO's links to be followed.
    @HasLink
    private MyChildPOJO myChildPOJO;
    
    
    /* Automatically followed links -> They are at the parent level scope */
    @Link
    private String link;
    
    @LinkObject("link")
    private MyLinkParentPOJO myLinkParentPOJO;
    
    // some other stuffs here
}

public class MyChildPOJO {
    
    
    /* Followed links -> Parent POJO has a @HasLink annotation on its MyChildPOJO field */
    @Link
    private String link;
    
    @LinkObject("link")
    private MyLinkParentPOJO myLinkParentPOJO;
    
    // some other stuffs here
    
}

public class MyLinkParentPOJO {
    

    // MyLinkChildPojo links won't be followed : no `@HasLink` annotation.
    private MyLinkChildPOJO myLinkChildPOJO;
                                  
                                  
    /* 
        Automatically followed links -> They are at the parent level scope 
        In fact following a link is considered as a new scrap so that the 
        mapped POJO type becomes a parent pojo scope has well. All of its
        links will be followed as default
    */
    @Link
    private String link;
                                  
    @LinkObject("link")
    private MyLinkParentPOJO2 myLinkParentPOJO2;
                                  
    // some other stuffs here                                  
                                  
}

public class MyLinkChildPOJO {
    
    // Won't be followed because MyLinkParentPOJO does not have @HasLink 
    // annotation on top of its MyLinkChildPOJO field...
    @Link
    private String link;
                                  
    @LinkObject("link")
    private MyLinkParentPOJO2 myLinkParentPOJO2;
    
    // some other stuffs here
    
}

public class MyLinkParentPOJO2 {
    // some stuffs in here
}

```

## Warning Signs

### General Knowledge

One common thing happening when scrapping is hitting false positive or security
barrier such as captchas. The idea behind Warning Sign is that we had to provide
a standard API to spot those signs and to choose the correct decision to handle
it properly.

The warning sign API works with a simple `@WarningSign` annotation to put on
top of one of your POJO's field. Now you have three more things to define on this
annotation :

- What is the warning sign exactly ? How and when will be triggered ? [see 
Triggered On](#triggered-on).
- What is the [action to take](#action) when triggered ?
- Should we also pause the current scrap ? Pause all of our current scraps ?
You can customize this following the instructions [here](#pausing-behavior).


**Warning!** It only works properly when using HtmlToPojoEngine provided by
the `AutomaticScrapperManagerBuilder` or when creating an instance using :

```java
HtmlToPojoEngine.create(new ScrapperHtmlAdapterFactory());
```
 
Otherwise, it won't work and will result in a class cast exception because
the scrapper API waits for a `ScrapperHtmlAdapterFactory` instead of the
default implementation of an `HtmlAdapterFactory` provided by jwht-htmltopojo
library. 

### Triggered On

There is yet five main triggers defined by `TriggeredOn` enum :

```java
public enum TriggeredOn {

    /**
     * When the field had a null or equivalent value. Null or Equivalent values
     * are : null, empty string or empty list.
     */
    NULL_VALUE,

    /**
     * When the field has the default value defined by {@link Selector#defValue()}.
     * Essentially, this means that the corresponding HTML element wasn't found and
     * that the default value was picked or that the element was found and had the 
     * default value.
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

```

Essentially, `TriggeredOn` works with `Selector` annotation from jwht-htmltopojo lib.
Let's give a practical exemple of how to use it :

We'll assume normal web page looks like this :

```html
<h1>This is a normal POJO page</h1>
<p class="name">The POJO is called <span>Paul</span></p>
```

And this is the same web page when a captcha was found :

```html
<h1>This is not a normal POJO page with some surprise captcha</h1>
<div class="captcha">
    <p>Are you a ROBOT??</p>
</div>
```

```java

public class MyPOJO {
    
    // We want to have "Paul" as the Pojo name
    @Selector(value = "p.name > span")
    private String pojoName;
    
    // If there is no captcha, then, it means that the selector resulting
    // value will be the default "NO_VALUE", otherwise, it will be 
    // "Are you a ROBOT??" and will match the warning sign regex.
    @Selector(value = "div.captcha > p")
    @WarningSign(
            triggeredOn = TriggeredOn.ANY_VALUE_MATCHING_REGEX,
            triggeredOnRegex = "^Are you a ROBOT\\?\\?$"
            // some other values below
    )
    private String warningCaptcha;
    
    // some other stuffs
}

```

How does this work ? When the page is the normal page, `div.captcha > p` css 
selector returns an empty element which is then resolved to the default value
`"NO_VALUE"`. This string obviously doesn't match our regex `^Are you a ROBOT\?\?$`
so no warning sign is triggered when the page is normal. But when the captcha
appears, `div.captcha > p` css selector is resolved to `Are you a ROBOT??` which
itself matches our regex `^Are you a ROBOT\?\?$`! Yup when the captcha appears, 
our warning sign will be triggered! 

Now the question you have in mind is probably : "Awesome but what will it do now?".
That's up to you! Will get into the details of it right below.

### Action

The second thing to define with your warning sign is the action to take when it 
happends. The Action enum describe the following actions :

```java
public enum Action {

    /**
     * Retry the request.
     */
    RETRY,

    /**
     * Throw an exception. This will have the same impact
     * as {@link #STOP_ACTUAL_SCRAP} except that the scrap
     * will be accounted as a failure, and if {@link Scrapper#throwExceptions()}
     * is set to true, it will completely stop the whole
     * scrapping operation.
     */
    THROW_EXCEPTION,

    /**
     * Stop the actual scrap without further links explored
     * and returned the current model in its actual state.
     */
    STOP_ACTUAL_SCRAP,

    /**
     * Won't do anything : the scrap will pursue where it was
     * without our current POJO being further analyzed. It will
     * act almost as with {@link #STOP_ACTUAL_SCRAP} except that
     * if the current scrapped POJO was itself a link followed,
     * the parent POJO scrap will continue.
     */
    NONE
}
```

Now you can just choose the action to perform depending on your needs.

### Pausing Behavior

Last parameter you can set is the `PausingBehavior`. Different pausing
behaviors can be choosen and are defined within the `PausingBehavior` 
enum :

```java
public enum PausingBehavior {

    /**
     * Won't wait at all.
     */
    DONT_WAIT,

    /**
     * Will pause current failed scrap for a time delay
     * specified by {@link RequestsConfig#warningSignDelay()}.
     *
     */
    PAUSE_CURRENT_THREAD_ONLY,

    /**
     * Will pause all current running scraps for a time delay
     * specified by {@link RequestsConfig#warningSignDelay()}.
     * Once the delay is over, the scraps will progressively
     * start back.
     */
    PAUSE_ALL_THREADS
}
```

## Scraper Client

### Introduction

The scrapper client is the client you will interact with to start
your scrapping, queue new objects to scrap, terminate, see metrics...

It can be created, as explained in the [Let's Scrap section](#lets-scrap) :

```java
AutomaticScrapperManager scrapperManager = new AutomaticScrapperManagerBuilder().build();

AutomaticScrapperClient scrapperClient = 
        scrapperManager.createClient(
            uncompletedRestaurants,
            new CoolRestaurantsScrapperHelper()
        );
```

One single scrapper can only be started once otherwise it will throw an exception.
It is not advised to run several scrapper for the same scrapping purpose concurrently
as you can choose your parrallel threads count from your [configurations](#parameter-parallelthreads-).

To start your scrapper client, use :

```java
scrapperManager.scrap();
```

### Queuing Elements

While the scrapper client is still running and while there are still objects to 
scrap in its queue, you can populate safely the queue with new elements. If the 
queue is already empty, you will receive an exception.

To fill the queue, use :

```java
scrapperManager.add( myAdditionalUncompletedRestaurants );
```

### Terminate

If you want to manually stop the current scrap and all of its subsequent currently
running threads while emptying the queue, you can terminate the scrapper this way :

```java
scrapperManager.terminate();
```

### Get Results

There are three different ways to get the results of the scrap and one way to 
know when the scrapping client has finished its work.

```java
scrapperManager.isCompleted();
```

Will tell you when the scrapping process is completed.

```java
scrapperManager.getResults();
```

Will either return the results of the scrap if the scrapping client has finished its work,
or throw a `ScrapperNotFinishedException` otherwise.

```java
scrapperManager.getResults(10, TimeUnit.MINUTES);
```

Will either return the results or throw a `ScrapperNotFinishedException` if the task
is not completed after the timeout is expired. **Warning!** If you use this method and
the timeout is reached, the scrapping client will terminate and close all remaining 
running scraps!

```java
scrapperManager.waitAndGetResults();
```

Will synchronously wait for the current scrapper client to end its task and return
its results.

### Get Metrics

The scrapper client is able to retrieve two metrics :

#### Scrapping Stats

- `ScrappingStats` which provides metrics about the current completion rates of
the scrapper client, failed and successful scraps count, running tasks, remaining
tasks.... [see documentation](https://github.com/whimtrip/jwht-scrapper/blob/master/src/main/java/fr/whimtrip/ext/jwhtscrapper/intfr/ScrappingStats.java).

#### Http Metrics

- `HttpMetrics` provides a more granular point of view with metrics about failed 
requests (which is different from failed scraps because the requests can be retried),
successful requests, http status... [see documentation](https://github.com/whimtrip/jwht-scrapper/blob/master/src/main/java/fr/whimtrip/ext/jwhtscrapper/intfr/HttpMetrics.java).


Both of those metrics can be retrieved at anytime whatever is the current status of the
scrapper client.

## Logging

The framework is completely logged using sl4j. We used four levels of logging :
`TRACE`, `DEBUG`, `INFO` and `WARNING`. When testing your scrapper for the first
time, we advise to use `TRACE` so that you can check clearly what happens under
the hood and which part of your scrapper implementation could be improved /
leads to bad results. When your scrapper will be ready for production, using
`INFO` logging is advised because `TRACE` and `DEBUG` both costs quite a few 
in terms of CPU usage.

All classes of this project belongs to `fr.whimtrip.ext.jwhtscrapper` so you can add
for example to your `logback.xml` :

```xml
<logger name="fr.whimtrip.ext.jwhtscrapper" level="DEBUG"/>
```

## Other Features

There are some more features we did not showcased here, especially within the scope
of [jwht-htmltopojo](https://www.github.com/whimtrip/jwht-htmltopojo) library. We
recommend to explore its features, especially those about [Field injection](https://github.com/whimtrip/jwht-htmltopojo#injection)
which proves to be useful when using jwht-scrapper library for complex scrapping
purposes.

## Overriding / Extending Standard API

The standard API of this library was fully interfaced so you can easily interact
with it. The best place to start with is the Builders provided throughout this 
library to build the different complex processing units of this library that you
could extends, rewrite...

# Upcoming Additions

At the moment I am writing those lines, the main thing that
needs to be added to this project is correct Unit Tests. Because
of a lack of time, we couldn't provide real Unit Tests. This 
is the first thing we want to add to this library.

Please feel free to submit your suggestions.


# How to contribute

If you find a bug, an error in the documentation or any other 
related problem, you can submit an issue or even propose a 
patch. 

Your pull requests will be evaluated properly but please submit
decent commented code we won't have to correct and rewrite from
scratch.

We are open to suggestions, code rewriting for optimization, 
etc...

If anyone wants to help, we'd really appreciate if related Unit
tests could be written first and before all to avoid further 
problem.

Thanks for using jwht-scrapper! Hope to hear from you!



        
    