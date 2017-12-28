# webdriver-manager
[![Build Status](https://travis-ci.com/nscuro/webdriver-manager.svg?token=24qz67tadxUHqtNZeoJu&branch=master)](https://travis-ci.com/nscuro/webdriver-manager)

*Simplifying WebDriver instantiation*

## Introduction
This project aims to simplify the process of constructing a `WebDriver` instance in Selenium test cases
by taking care of downloading the required binaries at runtime and unifying the way instanced are created.

## Supported Browsers
- [x] Google Chrome ([chromedriver](https://sites.google.com/a/chromium.org/chromedriver/))
- [x] Mozilla Firefox ([geckodriver](https://github.com/mozilla/geckodriver/releases))
- [x] Opera
- [ ] PhantomJS
- [ ] Microsoft Internet Explorer
- [ ] Microsoft Edge

## Usage

### Downloading Binaries
TODO

### Instantiating WebDriver
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

// ... 

@After
public void tearDown() {
    Optional.ofNullable(webDriver).ifPresent(WebDriver::quit);
}
```
Be aware that `WebDriverFactory` won't take care of closing your `WebDriver` instance, you **have** to do this yourself.
