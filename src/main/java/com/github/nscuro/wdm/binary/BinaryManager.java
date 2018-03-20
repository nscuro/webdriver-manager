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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * @since 0.2.0
 */
public interface BinaryManager {

    /**
     * Get a WebDriver binary.
     *
     * @param browser      The {@link Browser} to get the WebDriver binary for
     * @param version      The version of the WebDriver binary to get.
     *                     When {@code null}, the latest version will be used
     * @param os           The {@link Os} the binary must be compatible with
     * @param architecture The {@link Architecture} the binary must be compatible with
     * @return The WebDriver binary
     * @throws IOException In case of a network or file error
     */
    @Nonnull
    File getWebDriverBinary(final Browser browser, @Nullable final String version, final Os os, final Architecture architecture) throws IOException;

    /**
     * Register a given {@link File} as the WebDriver binary for a given {@link Browser}.
     *
     * @param browser             The {@link Browser} to register the WebDriver binary for
     * @param webDriverBinaryFile The binary {@link File} to register
     * @throws IllegalArgumentException      When the given {@link File} does not exist or is a directory
     * @throws UnsupportedOperationException When the given {@link Browser} does not support WebDriver binaries
     */
    void registerWebDriverBinary(final Browser browser, final File webDriverBinaryFile);

    /**
     * @return All locally stored WebDriver binary {@link File}s
     */
    @Nonnull
    List<File> getLocalWebDriverBinaries();

    /**
     * Get the latest WebDriver binary ({@link Os} and {@link Architecture} will be auto-detected).
     *
     * @param browser The {@link Browser} to get the WebDriver binary for
     * @return The WebDriver binary
     * @throws IOException In case of a network or file error
     */
    @Nonnull
    default File getLatestWebDriverBinary(final Browser browser) throws IOException {
        return getWebDriverBinary(browser, null, Os.getCurrent(), Architecture.getCurrent());
    }

    /**
     * Get a specific version of a WebDriver binary ({@link Os} and {@link Architecture} will be auto-detected).
     *
     * @param browser The {@link Browser} to get the WebDriver binary for
     * @param version The version of the WebDriver binary to get.
     *                When {@code null}, the latest version will be used
     * @return The WebDriver binary
     * @throws IOException In case of a network or file error
     */
    @Nonnull
    default File getWebDriverBinary(final Browser browser, final String version) throws IOException {
        return getWebDriverBinary(browser, version, Os.getCurrent(), Architecture.getCurrent());
    }

    /**
     * Get the latest WebDriver binary.
     *
     * @param browser      The {@link Browser} to get the WebDriver binary for
     * @param os           The {@link Os} the binary must be compatible with
     * @param architecture The {@link Architecture} the binary must be compatible with
     * @return The WebDriver binary
     * @throws IOException In case of a network or file error
     */
    @Nonnull
    default File getLatestWebDriverBinary(final Browser browser, final Os os, final Architecture architecture) throws IOException {
        return getWebDriverBinary(browser, null, os, architecture);
    }

    /**
     * Get the default {@link BinaryManager}.
     * <p>
     * Use this instead of {@link #builder()} if you don't need to perform any
     * customizations.
     * <p>
     * Downloaded binaries will be stored in {@code $HOME/.webdriver-manager}.
     *
     * @return A {@link BinaryManager} instance
     */
    @Nonnull
    static BinaryManager createDefault() {
        return builder()
                .defaultHttpClient()
                .defaultBinaryDestinationDir()
                .addBinaryProvider(ChromeDriverBinaryProvider::new)
                .addBinaryProvider(MicrosoftWebDriverBinaryProvider::new)
                .addBinaryProvider(GeckoDriverBinaryProvider::new)
                .addBinaryProvider(IEDriverServerBinaryProvider::new)
                .addBinaryProvider(OperaChromiumDriverBinaryProvider::new)
                .build();
    }

    @Nonnull
    static Builder.HttpClientStep builder() {
        return httpClient -> binaryDestinationDirPath -> new Builder(httpClient, binaryDestinationDirPath);
    }

    final class Builder {

        @FunctionalInterface
        public interface HttpClientStep {
            BinaryDestinationDirStep httpClient(final HttpClient httpClient);

            default BinaryDestinationDirStep defaultHttpClient() {
                return httpClient(HttpClients.custom()
                        .setUserAgent("webdriver-manager/0.2.0")
                        .disableAuthCaching()
                        .disableCookieManagement()
                        .build());
            }
        }

        @FunctionalInterface
        public interface BinaryDestinationDirStep {
            Builder binaryDestinationDir(final Path binaryDestinationDirPath);

            /**
             * Use the default binary destination dir ({@code $HOME/.webdriver-manager}).
             *
             * @return A {@link Builder} instance
             */
            default Builder defaultBinaryDestinationDir() {
                return binaryDestinationDir(Paths
                        .get(System.getProperty("user.home"))
                        .resolve(".webdriver-manager"));
            }
        }

        private final HttpClient httpClient;

        private final Path binaryDestinationDirPath;

        private final Set<BinaryProvider> binaryProviders;

        private Builder(final HttpClient httpClient,
                        final Path binaryDestinationDirPath) {
            this.httpClient = httpClient;
            this.binaryDestinationDirPath = binaryDestinationDirPath;
            this.binaryProviders = new HashSet<>();
        }

        @Nonnull
        public BinaryManager build() {
            return new BinaryManagerImpl(binaryDestinationDirPath, binaryProviders);
        }

        @Nonnull
        public Builder addBinaryProvider(final BinaryProvider binaryProvider) {
            this.binaryProviders.add(binaryProvider);
            return this;
        }

        @Nonnull
        public Builder addBinaryProvider(final Function<HttpClient, BinaryProvider> binaryProvider) {
            return addBinaryProvider(binaryProvider.apply(this.httpClient));
        }

    }

}
