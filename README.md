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

- [Proxy support](#usingproxies)
- [Warning Sign Support to detect captchas and more](#warningsigns)
- [Multithreading](#configurations)
- [Various scrapping delays when required](#configurations)
- [Rotating User-Agent](#configurations)
- [Request auto retry and HTTP redirections supports](#configurations) 
- [HTTP headers, cookies and more support](#configurations)
- [GET and POST support](#configurations)
- [Annotation Configuration](#configurations)
- [Detailed Scrapping Metrics](#metrics)
- [Async handling of the scrapper client](#scrapperclient)
- [jwht-htmltopojo fully featured framework to map HTML to POJO](https://github.com/whimtrip/jwht-htmltopojo)
- [Custom Input Format handling and built in JSON -> POJO mapping](#objectmapers)
- [Full Exception Handling Control](#exceptionhandling)
- [Detailed Logging with log4j](#logging)
- [Complex link following features](#linking)
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
        requestConfig =
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
public class CoolRestaurantScrapperHelper implements ScrapperHelper<UncompletedRestaurant, Restaurant>{

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

AutomaticScrapperManager scrapperManager = 
        new AutomaticScrapperManagerBuilder()
            // if you're scrapping an API instead of HTML pages, 
            // you could use the following config :
            //.setJsonScrapper(true)
            .build();

AutomaticScrapperClient scrapperClient = 
        scrapperManager.createClient(
            myPojosToInstanciateScrapping,
            new MyCustomHelperClass()
        );

// Start the scrapping in a separate thread
scrapperClient.scrap();


// Now just wait for your scrap to end!
// You can also terminate the scrapper, get its results with or without a timeout, get some
// statistics about the scrapping...

```
    