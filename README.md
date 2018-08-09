# jwht-scrapper - Fully Featured Java Scrapping Framework, highly pluggable and customizable

# Introduction
This lib provides a lightweight, fully featured, highly pluggable 
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
- [Complex link following features](#links)
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

Your scrapper client configuration can also be tuned and customised altought it is
a much more complex task. To do so, you need to input below described implementations
through the `AutomaticScrapperManagerBuilder` class. 

This describe all of the setters you can use as shown below :

```java
AutomaticScrapperManager scrapperManager = 
        new AutomaticScrapperManagerBuilder()
             //.setExceptionLogger(...)
             //.setCustomObjectMapper(...)
             //...
            .build();
```

#### Exception Logger

You can submit your own `ExceptionLogger` implementation here. this provides
a way to use a custom processing for exception handling. For our own implementation,
we used to saved the exceptions in the database, trigger an alarm so that the uncaught 
exception can be corrected as soon as possible, and finally log the stacktrace. This
is just an example of what you can do with such exception logger service.

#### HtmlToPojoEngine

You can provide your own `HtmlToPojoEngine` implementation altough it is really
one of the hardest and most dangerous customisation operation as it might clash 
with current implementations contracts.

If you are just providing your own `HtmlToPojoEngine` instance using the standard 
API, then this won't cause any problem and could be useful to reuse previously analysed
POJOs.

#### Custom Object Mapper

If you do not plan to receive HTML body responses from the scraps to perform, then 
you can use a custom object mapper implementation `public MyObMapper implements BasicObjectMapper`
to map other input formats to POJOs (Link annotations are still supported but not 
warning signs so you can annotate your POJOs with [links annotations](#links)).
If you do :

```java

        new AutomaticScrapperManagerBuilder()
            .setJsonScraper(true)
            // ...
            .build();
```

Then a Jackson object mapper will be used to turn proper Jackson annotated POJOs 
from JSON string.

#### Custom Async Http Client

If you want to set special default properties for your `AsyncHttpClient`, you
can submit your own instance.

#### Proxy Finder

If you want to use proxies, you have to set here your own proxy finder implementation.
This is a class instance that can retrieve proxies from a given source (usually a database)
modify their status to mark them as frozen or banned proxies for example, and finally
persisting them to the database when necessary. When implementing `ProxyFinder` with 
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

### @Link

#### @LinkObject

#### @LinkObjects

### @LinkListsFromBuilder

### @HasLink

## Warning Signs

### General Knowledge

### Triggered On

### Action

### Pausing Behavior

## Scraper Client

### Queuing Elements

### Terminate

### Get Results

### Get Metrics

#### Where to retrieve it

#### Scrapping Stats

#### Http Metrics

## Logging

## Other Features

### Pojo Injection

## Overriding / Extending Standard API

Overriding the Standard API can be made in several ways.
The most easy one is to ... TODO

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



        
    