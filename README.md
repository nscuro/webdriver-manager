# webdriver-manager
[![Build Status](https://travis-ci.org/nscuro/webdriver-manager.svg?branch=master)](https://travis-ci.org/nscuro/webdriver-manager)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.nscuro/webdriver-manager/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.nscuro/webdriver-manager)
[![JavaDoc](http://javadoc-badge.appspot.com/com.github.nscuro/webdriver-manager.svg?label=javadoc)](https://nscuro.github.com/webdriver-manager/javadoc)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Coverage Status](https://coveralls.io/repos/github/nscuro/webdriver-manager/badge.svg?branch=master)](https://coveralls.io/github/nscuro/webdriver-manager?branch=master)

*Simplifying WebDriver instantiation*

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
- [ ] PhantomJS
- [ ] Microsoft Internet Explorer
- [x] Microsoft Edge ([Microsoft WebDriver](https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/))
 
Of course, browsers that do not require a separate driver binary are also supported:
- [x] HtmlUnit
- [x] Safari

## Setup
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

<!-- Only required if you plan on using PhantomJS -->
<dependency>
    <groupId>com.codeborne</groupId>
    <artifactId>phantomjsdriver</artifactId>
    <version>${phantomjs-driver.version}</version>
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
Binaries can be downloaded using the `BinaryManager` class:
```java
final BinaryManager binaryManager = BinaryManager.builder()
    .defaultHttpClient() // you can provide your own using .httpClient(myHttpClient)
    .addChromeDriverBinaryDownloader()
    // .addDefaultBinaryDownloaders()
    // .addBinaryDownloader(myCustomBinaryDownloader)
    .build();

// Get a specific version for specific platform
final File binaryFile = binaryManager.getBinary(Browser.CHROME, "2.33", Os.WINDOWS, Architecture.X64);
```
Binaries will be downloaded to `$HOME/.webdriver-manager` and can be programmatically deleted 
using `binaryManager.cleanUp()`.

#### GitHub API
Some binaries are being downloaded from GitHub (currently Firefox's `geckodriver` & Opera's `operachromiumdriver`).
GitHub **may** limit the amount of requests being performed against their API, in which case you must
provide OAuth credentials in order to authorize yourself.

Currently, you need to set the `WDM_GH_USER` and `WDM_GH_TOKEN` **ENVIRONMENT** variables for
this to work - where `WDM_GH_USER` is your GitHub username and `WDM_GH_TOKEN` is a *personal access token*
whith the permission to access public repositories:

![personal access token](https://i.imgur.com/Lm6cWAN.png)

```bash
export WDM_GH_USER=<your-github-username>
export WDM_GH_TOKEN=<your-token>
```

### Instantiating WebDriver

#### Instantiating WebDriver locally
In your test or testing framework, you'd typically do the following:
```java
private static WebDriverFactory webDriverFactory;
 
private WebDriver webDriver;
 
@BeforeClass
public static void setUpClass() {
    webDriverFactory = new LocalWebDriverFactory(BinaryManager.createDefault());
}
 
@Before
public void setUp() {
    webDriver = webDriverFactory.getWebDriver(new ChromeOptions());
}
 
@Test
public void testSomeStuff() {
    webDriver.get("...");
    // ...
}
 
@After
public void tearDown() {
    Optional.ofNullable(webDriver).ifPresent(WebDriver::quit);
}
```
Be aware that `WebDriverFactory` won't take care of closing your `WebDriver` instance, you **have** to do this yourself.

##### Specifying binary versions
Per default, `LocalWebDriverFactory` will always download the latest version of a webdriver binary.
This behavior may not always be what you want - i.e. you use an older browser version or the latest binary version
does not support your system's architecture anymore.

In order to solve this issue, you can provide a `WebDriverFactoryConfig` to the `WebDriverFactory` in which you explicitly
state which version shall be downloaded:
```java
final WebDriverFactoryConfig config = new WebDriverFactoryConfig();
config.setBinaryVersionForBrowser(Browser.CHROME, "2.32");
 
final WebDriverFactory webDriverFactory = new LocalWebDriverFactory(BinaryManager.createDefault(), config);
```

#### Instantiating WebDriver remotely
Additionally to the local instantiation, you can use `WebDriverFactory` with a remote Selenium Grid Hub:
```java
final WebDriverFactory webDriverFactory = new RemoteWebDriverFactory("http://my-grid-host:4444/wd/hub");
 
final WebDriver webDriver = webDriverFactory.getWebDriver(new FirefoxOptions());
```

Because you will connect to a remote machine, there's naturally no need to download any binaries (on your machine, that is).

## Managing WebDriver instances
TODO
