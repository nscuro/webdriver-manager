package com.github.nscuro.wdm.binary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.chrome.ChromeDriverBinaryDownloader;
import com.github.nscuro.wdm.binary.firefox.GeckoDriverBinaryDownloader;
import com.github.nscuro.wdm.binary.github.GitHubReleasesService;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public interface BinaryManager {

    /**
     * Get a webdriver binary for the given {@link Browser}, version, {@link Os} and {@link Architecture}
     * <p>
     * When a binary for the given requirements is found locally,
     * it will not be downloaded again.
     *
     * @param browser      The browser to download the binary for
     * @param version      The version of the binary (<strong>not</strong> the browser version)
     * @param os           The operating system the binary must be compatible with
     * @param architecture The architecture the binary must be compatible with
     * @return A {@link File} handle of the downloaded binary
     * @throws IOException When downloading the binary failed
     */
    @Nonnull
    File getBinary(final Browser browser, final String version, final Os os, final Architecture architecture) throws IOException;

    /**
     * Get a webdriver binary for the given {@link Browser} and version.
     * <p>
     * This assumes that a binary for the current {@link Os} and {@link Architecture} is requested.
     * <p>
     * When a binary for the given requirements is found locally,
     * it will not be downloaded again.
     *
     * @param browser The browser to download the binary for
     * @param version The version of the binary (<strong>not</strong> the browser version)
     * @return A {@link File} handle of the downloaded binary
     * @throws IOException When downloading the binary failed
     * @see #getBinary(Browser, String, Os, Architecture)
     */
    @Nonnull
    default File getBinary(final Browser browser, final String version) throws IOException {
        return getBinary(browser, version, Os.getCurrent(), Architecture.getCurrent());
    }

    /**
     * Get the latest webdriver binary for the given {@link Browser}, {@link Os} and {@link Architecture}.
     * <p>
     * When a binary for the given requirements is found locally,
     * it will not be downloaded again.
     *
     * @param browser      The browser to download the binary for
     * @param os           The operating system the binary must be compatible with
     * @param architecture The architecture the binary must be compatible with
     * @return A {@link File} handle of the downloaded binary
     * @throws IOException When downloading the binary failed
     * @see #getBinary(Browser, String, Os, Architecture)
     */
    @Nonnull
    default File getBinary(final Browser browser, final Os os, final Architecture architecture) throws IOException {
        return getBinary(browser, "latest", os, architecture);
    }

    /**
     * Get the latest webdriver binary for a given {@link Browser}.
     * <p>
     * This assumes that a binary for the current {@link Os} and {@link Architecture} is requested.
     * <p>
     * When a binary for the given requirements is found locally,
     * it will not be downloaded again.
     *
     * @param browser The browser to download the binary for
     * @return A {@link File} handle of the downloaded binary
     * @throws IOException When downloading the binary failed
     * @see #getBinary(Browser, String, Os, Architecture)
     */
    @Nonnull
    default File getBinary(final Browser browser) throws IOException {
        return getBinary(browser, "latest");
    }

    /**
     * Register a given binary file as the webdriver binary to use for a given {@link Browser}.
     *
     * @param binaryFile The binary file to register
     * @param browser    The {@link Browser} to register the binary for
     */
    void registerBinary(final File binaryFile, final Browser browser);

    static BinaryManager createDefault() {
        final HttpClient httpClient = HttpClients.custom()
                .setUserAgent("Mozilla/5.0")
                .disableAuthCaching()
                .disableRedirectHandling()
                .disableCookieManagement()
                .build();

        final GitHubReleasesService gitHubReleasesService = GitHubReleasesService
                .create(httpClient, new ObjectMapper());

        final BinaryDownloader chromeDownloader = new ChromeDriverBinaryDownloader(httpClient);

        final BinaryDownloader geckoDownloader = new GeckoDriverBinaryDownloader(httpClient, gitHubReleasesService);

        return new BinaryManagerImpl(new HashSet<>(Arrays.asList(chromeDownloader, geckoDownloader)));
    }

}
