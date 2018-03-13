package com.github.nscuro.wdm.binary;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.chrome.ChromeDriverBinaryProvider;
import com.github.nscuro.wdm.binary.edge.MicrosoftWebDriverBinaryProvider;
import com.github.nscuro.wdm.binary.firefox.GeckoDriverBinaryProvider;
import com.github.nscuro.wdm.binary.ie.IEDriverServerBinaryProvider;
import com.github.nscuro.wdm.binary.opera.OperaChromiumDriverBinaryProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @since 0.2.0
 */
public interface BinaryManager {

    @Nonnull
    File getWebDriverBinary(final Browser browser, @Nullable final String version, final Os os, final Architecture architecture) throws IOException;

    void registerWebDriverBinary(final Browser browser, final File webDriverBinaryFile);

    @Nonnull
    default File getWebDriverBinary(final Browser browser) throws IOException {
        return getWebDriverBinary(browser, null, Os.getCurrent(), Architecture.getCurrent());
    }

    @Nonnull
    default File getWebDriverBinary(final Browser browser, final String version) throws IOException {
        return getWebDriverBinary(browser, version, Os.getCurrent(), Architecture.getCurrent());
    }

    @Nonnull
    default File getWebDriverBinary(final Browser browser, final Os os, final Architecture architecture) throws IOException {
        return getWebDriverBinary(browser, null, os, architecture);
    }

    @Nonnull
    static BinaryManager createDefault() {
        final Path binaryDestinationDirPath = Paths
                .get(System.getProperty("user.home"))
                .resolve(".webdriver-manager");

        final HttpClient httpClient = HttpClients.custom()
                .setUserAgent("webdriver-manager/0.2.0")
                .disableAuthCaching()
                .disableCookieManagement()
                .build();

        final Set<BinaryProvider> binaryProviders = new HashSet<>(Arrays.asList(
                new ChromeDriverBinaryProvider(httpClient),
                new MicrosoftWebDriverBinaryProvider(httpClient),
                new GeckoDriverBinaryProvider(httpClient),
                new IEDriverServerBinaryProvider(httpClient),
                new OperaChromiumDriverBinaryProvider(httpClient)
        ));

        return new BinaryManagerImpl(binaryDestinationDirPath, binaryProviders);
    }

}
