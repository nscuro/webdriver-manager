# webdriver-manager
[![Build Status](https://github.com/nscuro/webdriver-manager/workflows/Continuous%20Integration/badge.svg)](https://github.com/nscuro/webdriver-manager/actions?query=workflow%3A%22Continuous+Integration%22)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.nscuro/webdriver-manager/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.nscuro/webdriver-manager)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![codecov](https://codecov.io/gh/nscuro/webdriver-manager/branch/master/graph/badge.svg?token=8Wy45EtrQz)](https://codecov.io/gh/nscuro/webdriver-manager)
![Development Status](https://img.shields.io/badge/development-discontinued-red)

*Simplifying WebDriver instantiation*

> ⚠ **Development of this library is discontinued** ⚠  
> I'm not working with Selenium anymore, so there's no reason for me to keep updating this

## Introduction
This project aims to simplify the process of constructing a `WebDriver` instance in [Selenium](https://github.com/SeleniumHQ/selenium) test cases
by taking care of downloading the required binaries at runtime and unifying the way instances are created.

Overall, there are 3 things `webdriver-manager` can do for you:
- [Downloading WebDriver binaries](#downloading-webdriver-binaries)
- [Instantiating WebDriver](#instantiating-webdriver)
- [Managing WebDriver instances](#managing-webdriver-instances)

## Supported Browsers
- [x] Google Chrome ([chromedriver](https://sites.google.com/a/chromium.org/chromedriver/))
- [x] Mozilla Firefox ([geckodriver](https://github.com/mozilla/geckodriver))
- [x] Opera ([operachromiumdriver](https://github.com/operasoftware/operachromiumdriver))
- [x] Microsoft Internet Explorer ([IEDriverServer](https://msdn.microsoft.com/en-us/library/dn800898(v=vs.85).aspx))
- [x] Microsoft Edge ([Microsoft WebDriver](https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/))
 
Of course, browsers that do not require a separate driver binary are also supported:
- [x] HtmlUnit
- [x] Safari

## Setup
*Java 8 or higher is required!*  

```xml
<dependency>
    <groupId>com.github.nscuro</groupId>
    <artifactId>webdriver-manager</artifactId>
    <version>${webdriver-manager.version}</version>
</dependency>
```
For the latest available version see [here](https://github.com/nscuro/webdriver-manager/releases).

Note that `webdriver-manager` requires you to provide the actual Selenium dependencies yourself:
```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>${selenium.version}</version>
</dependency>

<!-- Only required if you plan on using HTMLUnit -->
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>htmlunit-driver</artifactId>
    <version>${htmlunit-driver.version}</version>
</dependency>
```
Optimally, this allows you to update to a newer Selenium version without having to wait for
`webdriver-manager` to do it. As long as Selenium does not introduce breaking changes, this
should work just fine.

## Usage

### Downloading WebDriver binaries
Binaries can be downloaded using the [`BinaryManager`](https://nscuro.github.io/webdriver-manager/com/github/nscuro/wdm/binary/BinaryManager.html) class:
```java
BinaryManager binaryManager = BinaryManager.createDefault();

File chromeDriverBinary = binaryManager.getLatestWebDriverBinary(Browser.CHROME, Os.WINDOWS, Architecture.X64);
```
For more ways to download binaries please refer to the BinaryManager [documentation](https://nscuro.github.io/webdriver-manager/com/github/nscuro/wdm/binary/BinaryManager.html).

Using [`BinaryManager.createDefault()`](https://nscuro.github.io/webdriver-manager/com/github/nscuro/wdm/binary/BinaryManager.html#createDefault--) will provide you with a `BinaryManager` instance that
should be able to fulfill all basic needs. 

You can however use a builder for customization purposes:
```java
BinaryManager binaryManager = BinaryManager
    .builder()
    // A HttpClient MUST be provided. If you do not care about this step, use .defaultHttpClient()
    .httpClient(myHttpClient)
    // You can also use .defaultBinaryDestinationDir(), which will cause all downloaded
    // binaries to be placed in $HOME/.webdriver-manager
    .binaryDestinationDir(Paths.get("/home/darthvader/webdriver"))
    .addBinaryProvider(myCustomBinaryProvider)
    // When a BinaryProvider has a constructor that only takes a HttpClient,
    // you can pass it as method reference. The builder will then inject the HttpClient
    // that was chosen in the first builder step.
    .addBinaryProvider(ChromeDriverBinaryProvider::new) // For Google Chrome
    .addBinaryProvider(GeckoDriverBinaryProvider::new) // For Mozilla Firefox
    .build();
```

#### GitHub API
Some binaries are being downloaded from GitHub (currently Firefox's [`geckodriver`](https://github.com/mozilla/geckodriver) & Opera's [`operachromiumdriver`](https://github.com/operasoftware/operachromiumdriver)).
GitHub [limits](https://developer.github.com/v3/#rate-limiting) the amount of requests being performed against their API, in which case you must
provide [API credentials](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/) in order to authorize yourself.

Currently, you need to set the `WDM_GH_USER` and `WDM_GH_TOKEN` **ENVIRONMENT** variables for
this to work - where `WDM_GH_USER` is your GitHub username and `WDM_GH_TOKEN` is a *personal access token*
whith the permission to access public repositories:

![personal access token](https://i.imgur.com/Lm6cWAN.png)

```bash
export WDM_GH_USER=<your-github-username>
export WDM_GH_TOKEN=<your-token>
```

### Instantiating WebDriver

With vanilla Selenium, in order to get a `WebDriver` instance up and running you'd have to do the following:
```java
// Manually download the required binary, placing it somewhere on your filesystem...

System.setProperty("webdriver.chrome.driver", "/the/path/to/chromedriver.exe");

WebDriver webDriver = new ChromeDriver(new ChromeOptions());
```

Whereas if you'd want to get a remote instance from your Selenium Grid, it'd be as easy as:
```java
WebDriver webDriver = new RemoteWebDriver(new URI("http://my-grid-domain:4444/wd/hub"), new ChromeOptions());
```

What `webdriver-manager` offers you is a [simple interface](https://nscuro.github.io/webdriver-manager/com/github/nscuro/wdm/factory/WebDriverFactory.html) that reduces all this down to:
```java
// WebDriverFactory myWebDriverFactory = ...

WebDriver webDriver = myWebDriverFactory.createWebDriver(new ChromeOptions());
```

There are two implementations of this interface, but you are of course free to write your own.

#### Local instantiation

Using the [`LocalWebDriverFactory`](https://nscuro.github.io/webdriver-manager/com/github/nscuro/wdm/factory/LocalWebDriverFactory.html), you can create `WebDriver` instances on the current machine.  
It will automatically download any required binaries, as long as you provide a `BinaryManager` instance at construction:

```java
// With BinaryManager, the factory will be able to download required binaries
WebDriverFactory factory = new LocalWebDriverFactory(BinaryManager.createDefault());
WebDriver webDriver = factory.createWebDriver(new ChromeOptions());

// Without BinaryManager, the factory will not download any binaries
// Do this when you exclusively use Browsers that do not require a separate binary (e.g. HTMLUnit or Safari)
WebDriverFactory factory = new LocalWebDriverFactory();
WebDriver webDriver = factory.createWebDriver(DesiredCapabilities.htmlUnit());
```

##### Specifying binary versions

Per default, `LocalWebDriverFactory` will always download the latest version of a binary.
This behavior may not always be what you want - i.e. you use an older browser version or the latest binary version
does not support your system's architecture anymore.

In order to solve this issue, you can provide a [`WebDriverFactoryConfig`](https://nscuro.github.io/webdriver-manager/com/github/nscuro/wdm/factory/WebDriverFactoryConfig.html) in which you explicitly state which version shall be downloaded:
```java
WebDriverFactoryConfig config = new WebDriverFactoryConfig();
config.setBinaryVersionForBrowser(Browser.CHROME, "2.32");
config.setBinaryVersionForBrowser(Browser.FIREFOX, "v0.17.0");
 
WebDriverFactory factory = new LocalWebDriverFactory(BinaryManager.createDefault(), config);
```

#### Remote instantiation

Alternatively to the local instantiation, you can use [`RemoteWebDriverFactory`](https://nscuro.github.io/webdriver-manager/com/github/nscuro/wdm/factory/RemoteWebDriverFactory.html) with your Selenium Grid server:
```java
WebDriverFactory factory = new RemoteWebDriverFactory("http://my-grid-domain:4444/wd/hub");
 
WebDriver webDriver = factory.createWebDriver(new FirefoxOptions());
```

Because you are connecting to a remote machine, there's naturally no need to download any binaries (on your machine, that is).

## Managing WebDriver instances

Building upon the above `WebDriverFactory`, [`WebDriverManager`](https://nscuro.github.io/webdriver-manager/com/github/nscuro/wdm/manager/WebDriverManager.html)s are used to manage the created instances (keeping references to them, limiting overall instance count, making sure they're properly closed...).

### SingletonWebDriverManager

As the name suggests, this `WebDriverManager` manages a single `WebDriver` instance.
You request a `WebDriver` instance by calling the manager's [`getWebDriver()`](https://nscuro.github.io/webdriver-manager/com/github/nscuro/wdm/manager/SingletonWebDriverManager.html#getWebDriver-org.openqa.selenium.Capabilities-) method, 
it basically behaves like `WebDriverFactory`, except:

>  * If no WebDriver is currently active, a new instance will be created.
>  * If a WebDriver is currently active and its Capabilities match the given desired ones,
>      the currently active instance will be returned.
>  * If a WebDriver is currently active and its Capabilities DO NOT match the given
>      desired ones, the currently active instance will be closed and a new one will be created.

This is useful when your tests run sequentially, as an instance can now be reused as long as the requested
`Capabilities` stay the same. 

Example usage with JUnit Jupiter:

```java
class ExampleTest {
    
    private static WebDriverManager webDriverManager;
    
    private WebDriver webDriver;
    
    @BeforeAll
    static void beforeAll() {
        WebDriverFactory webDriverFactory = new LocalWebDriverFactory(BinaryManager.createDefault());
        
        webDriverManager = new SingletonWebDriverManager(webDriverFactory);
    }
    
    @BeforeEach
    void beforeEach() {
        webDriver = webDriverManager.getWebDriver(new ChromeOptions());
    }
    
    @Test
    void testDuckDuckGo() {
        webDriver.get("https://duckduckgo.com");
        
        // ...
    }
    
    @AfterAll
    static void afterAll() {
        webDriverManager.shutdown();
    }
    
}
```

For all available implementations, please refer to the [javadoc](https://nscuro.github.io/webdriver-manager/com/github/nscuro/wdm/manager/package-summary.html).
