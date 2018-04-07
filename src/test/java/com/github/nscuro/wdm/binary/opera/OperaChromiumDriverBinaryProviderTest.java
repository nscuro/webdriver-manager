package com.github.nscuro.wdm.binary.opera;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.Platform;
import com.github.nscuro.wdm.binary.BinaryProvider;
import com.github.nscuro.wdm.binary.util.compression.BinaryExtractorFactory;
import com.github.nscuro.wdm.binary.util.github.GitHubRelease;
import com.github.nscuro.wdm.binary.util.github.GitHubReleasesService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class OperaChromiumDriverBinaryProviderTest {

    private GitHubReleasesService gitHubReleasesServiceMock;

    private BinaryExtractorFactory binaryExtractorFactoryMock;

    private OperaChromiumDriverBinaryProvider binaryProvider;

    @BeforeEach
    void beforeEach() {
        gitHubReleasesServiceMock = mock(GitHubReleasesService.class);

        binaryExtractorFactoryMock = mock(BinaryExtractorFactory.class);

        binaryProvider = new OperaChromiumDriverBinaryProvider(gitHubReleasesServiceMock,
                binaryExtractorFactoryMock);
    }

    @Nested
    class ProvidesBinaryForBrowserTest {

        @Test
        void shouldReturnTrueForOperaBrowser() {
            assertThat(binaryProvider.providesBinaryForBrowser(Browser.OPERA)).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Browser.class, mode = Mode.EXCLUDE, names = "OPERA")
        void shouldReturnFalseForEveryBrowserExceptOpera(final Browser browser) {
            assertThat(binaryProvider.providesBinaryForBrowser(browser)).isFalse();
        }

    }

    @Nested
    class GetLatestBinaryVersionTest {

        @Test
        void shouldReturnLatestNormalizedVersion() throws IOException {
            final GitHubRelease oldReleaseMock = mock(GitHubRelease.class);

            given(oldReleaseMock.getTagName())
                    .willReturn("v.2.2");

            given(oldReleaseMock.hasAssetForPlatform(any(Platform.class)))
                    .willReturn(true);

            final GitHubRelease newReleaseMock = mock(GitHubRelease.class);

            given(oldReleaseMock.getTagName())
                    .willReturn("v2.29");

            given(oldReleaseMock.hasAssetForPlatform(any(Platform.class)))
                    .willReturn(true);

            given(gitHubReleasesServiceMock.getAllReleases())
                    .willReturn(Arrays.asList(
                            newReleaseMock,
                            oldReleaseMock
                    ));

            assertThat(binaryProvider.getLatestBinaryVersion(Os.WINDOWS, Architecture.X64))
                    .hasValue("2.29");
        }

        @Test
        void shouldReturnEmptyOptionalWhenPlatformIsNotSupported() throws IOException {
            assertThat(binaryProvider.getLatestBinaryVersion(Os.MACOS, Architecture.X86))
                    .isNotPresent();
        }

        @Test
        void shouldReturnEmptyOptionalWhenNoReleaseExists() throws IOException {
            given(gitHubReleasesServiceMock.getAllReleases())
                    .willReturn(Collections.emptyList());

            assertThat(binaryProvider.getLatestBinaryVersion(Os.LINUX, Architecture.X64))
                    .isNotPresent();
        }

        @Test
        void shouldReturnEmptyOptionalWhenPlatformDoesNotMatchAnyRelease() throws IOException {
            final GitHubRelease releaseMock = mock(GitHubRelease.class);

            given(releaseMock.getTagName())
                    .willReturn("doesNotMatter");

            given(releaseMock.hasAssetForPlatform(any(Platform.class)))
                    .willReturn(false);

            given(gitHubReleasesServiceMock.getAllReleases())
                    .willReturn(Collections.singletonList(releaseMock));

            assertThat(binaryProvider.getLatestBinaryVersion(Os.WINDOWS, Architecture.X64))
                    .isNotPresent();
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
                    return Browser.OPERA == browser;
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
    @DisplayName("should have the same hashCode as the Opera browser")
    void shouldHaveTheSameHashCodeAsChromeBrowser() {
        assertThat(binaryProvider.hashCode()).isEqualTo(Browser.OPERA.hashCode());
    }

}