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

    @Nonnull
    File getWebDriverBinary(final Browser browser, @Nullable final String version, final Os os, final Architecture architecture) throws IOException;

    void registerWebDriverBinary(final Browser browser, final File webDriverBinaryFile);

    @Nonnull
    List<File> getLocalWebDriverBinaries();

    @Nonnull
    default File getLatestWebDriverBinary(final Browser browser) throws IOException {
        return getWebDriverBinary(browser, null, Os.getCurrent(), Architecture.getCurrent());
    }

    @Nonnull
    default File getWebDriverBinary(final Browser browser, final String version) throws IOException {
        return getWebDriverBinary(browser, version, Os.getCurrent(), Architecture.getCurrent());
    }

    @Nonnull
    default File getLatestWebDriverBinary(final Browser browser, final Os os, final Architecture architecture) throws IOException {
        return getWebDriverBinary(browser, null, os, architecture);
    }

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
