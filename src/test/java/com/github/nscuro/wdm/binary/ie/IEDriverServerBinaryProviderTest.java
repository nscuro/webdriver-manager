package com.github.nscuro.wdm.binary.ie;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryProvider;
import com.github.nscuro.wdm.binary.util.compression.BinaryExtractorFactory;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageDirectoryService;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class IEDriverServerBinaryProviderTest {

    private static final String PLATFORM_WIN32 = "Win32";

    private static final String PLATFORM_X64 = "x64";

    private GoogleCloudStorageDirectoryService cloudStorageDirectoryMock;

    private BinaryExtractorFactory binaryExtractorFactoryMock;

    private IEDriverServerBinaryProvider binaryProvider;

    @BeforeEach
    void beforeEach() {
        cloudStorageDirectoryMock = mock(GoogleCloudStorageDirectoryService.class);

        binaryExtractorFactoryMock = mock(BinaryExtractorFactory.class);

        binaryProvider = new IEDriverServerBinaryProvider(cloudStorageDirectoryMock, binaryExtractorFactoryMock);
    }

    @Nested
    class ProvidesBinaryForBrowserTest {

        @Test
        void shouldReturnTrueForInternetExplorerBrowser() {
            assertThat(binaryProvider.providesBinaryForBrowser(Browser.INTERNET_EXPLORER)).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Browser.class, mode = Mode.EXCLUDE, names = "INTERNET_EXPLORER")
        void shouldReturnFalseForEveryBrowserExceptInternetExplorer(final Browser browser) {
            assertThat(binaryProvider.providesBinaryForBrowser(browser)).isFalse();
        }

    }

    @Nested
    class GetLatestBinaryVersionTest {

        @Test
        void shouldReturnLatestVersion() throws IOException {
            given(cloudStorageDirectoryMock.getEntries())
                    .willReturn(Arrays.asList(
                            new GoogleCloudStorageEntry(format("1.0/IEDriverServer_%s", PLATFORM_WIN32), null),
                            new GoogleCloudStorageEntry(format("1.1/IEDriverServer_%s", PLATFORM_X64), null),
                            new GoogleCloudStorageEntry(format("1.2/IEDriverServer_%s", PLATFORM_WIN32), null),
                            // This entry shouldn't match as it doesn't contain the binary name
                            new GoogleCloudStorageEntry(format("1.3/%s", PLATFORM_X64), null)
                    ));

            assertThat(binaryProvider.getLatestBinaryVersion(Os.WINDOWS, Architecture.X86))
                    .hasValue("1.2");

            assertThat(binaryProvider.getLatestBinaryVersion(Os.WINDOWS, Architecture.X64))
                    .hasValue("1.1");
        }

        @ParameterizedTest
        @EnumSource(value = Os.class, mode = Mode.EXCLUDE, names = "WINDOWS")
        void shouldReturnEmptyOptionalForEveryOsExceptWindows(final Os os) throws IOException {
            assertThat(binaryProvider.getLatestBinaryVersion(os, Architecture.X86)).isNotPresent();

            assertThat(binaryProvider.getLatestBinaryVersion(os, Architecture.X64)).isNotPresent();
        }

    }

    @Nested
    class DownloadTest {

        @ParameterizedTest
        @EnumSource(value = Os.class, mode = Mode.EXCLUDE, names = "WINDOWS")
        @SuppressWarnings("Duplicates")
        void shouldThrowExceptionForEveryOsExceptWindows(final Os os) {
            final String version = "doesNotMatter";

            final Path mockedPath = mock(Path.class);

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> binaryProvider.download(version, os, Architecture.X86, mockedPath));

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> binaryProvider.download(version, os, Architecture.X64, mockedPath));
        }

        @Test
        void shouldThrowExceptionWhenDesiredVersionDoesNotExist() throws IOException {
            given(cloudStorageDirectoryMock.getEntries())
                    .willReturn(Collections.emptyList());

            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> binaryProvider.download("doesNotMatter", Os.WINDOWS, Architecture.X86, mock(Path.class)));
        }

    }

    @Nested
    @DisplayName("when checking equality")
    class EqualsTest {

        @Test
        @DisplayName("should return false when being compared with null")
        void shouldReturnFalseWhenComparedWithNull() {
            assertThat(binaryProvider.equals(null)).isFalse();
        }

        @Test
        @DisplayName("should return true when being compared with itself")
        void shouldReturnTrueWhenComparedWithItself() {
            assertThat(binaryProvider.equals(binaryProvider)).isTrue();
        }

        @Test
        @DisplayName("should return false when being compared with object that is not a BinaryProvider")
        void shouldReturnFalseWhenComparedWithNonBinaryProvider() {
            assertThat(binaryProvider.equals("noBinaryProvider")).isFalse();
        }

        @Test
        @DisplayName("should return true when being compared with a BinaryProvider that provides binaries for the same browser")
        void shouldReturnTrueWhenComparedWithBinaryProviderThatProvidesBinaryForSameBrowser() {
            final BinaryProvider otherBinaryProvider = new BinaryProvider() {
                @Override
                public boolean providesBinaryForBrowser(final Browser browser) {
                    return Browser.INTERNET_EXPLORER == browser;
                }

                @Nonnull
                @Override
                public Optional<String> getLatestBinaryVersion(final Os os, final Architecture architecture) {
                    throw new UnsupportedOperationException();
                }

                @Nonnull
                @Override
                public File download(final String version, final Os os, final Architecture architecture, final Path binaryDestinationPath) {
                    throw new UnsupportedOperationException();
                }
            };

            assertThat(binaryProvider.equals(otherBinaryProvider)).isTrue();
        }

    }

    @Test
    @DisplayName("should have the same hashCode as the Internet Explorer browser")
    void shouldHaveTheSameHashCodeAsChromeBrowser() {
        assertThat(binaryProvider.hashCode()).isEqualTo(Browser.INTERNET_EXPLORER.hashCode());
    }

}