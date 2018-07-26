# jwht-scrapper
Fully Featured Java Scrapping Framework, highly pluggable and customizable



## Quick Start :

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
    