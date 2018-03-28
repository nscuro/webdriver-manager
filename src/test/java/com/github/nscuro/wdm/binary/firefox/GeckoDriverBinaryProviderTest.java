package com.github.nscuro.wdm.binary.firefox;

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

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("The GeckoDriver BinaryProvider")
class GeckoDriverBinaryProviderTest {

    private GitHubReleasesService gitHubReleasesServiceMock;

    private BinaryExtractorFactory binaryExtractorFactoryMock;

    private GeckoDriverBinaryProvider binaryProvider;

    @BeforeEach
    void beforeEach() {
        gitHubReleasesServiceMock = mock(GitHubReleasesService.class);

        binaryExtractorFactoryMock = mock(BinaryExtractorFactory.class);

        binaryProvider = new GeckoDriverBinaryProvider(gitHubReleasesServiceMock,
                binaryExtractorFactoryMock);
    }

    @Nested
    @DisplayName("when being instantiated")
    class PublicConstructorTest {

        @Test
        @DisplayName("should throw an exception when no HttpClient was provided")
        void shouldThrowExceptionWhenNoHttpClientIsProvided() {
            assertThatExceptionOfType(NullPointerException.class)
                    .isThrownBy(() -> new GeckoDriverBinaryProvider(null));
        }

    }

    @Nested
    @DisplayName("when indicating browser support")
    class ProvidesBinaryForBrowserTest {

        @Test
        @DisplayName("should return true for Firefox")
        void shouldReturnTrueForFirefoxBrowser() {
            assertThat(binaryProvider.providesBinaryForBrowser(Browser.FIREFOX)).isTrue();
        }

        @ParameterizedTest(name = "[{index}] browser={0}")
        @EnumSource(Browser.class)
        @DisplayName("should return false for every browser except Edge")
        void shouldReturnFalseForEveryBrowserExceptFirefox(final Browser browser) {
            assumeFalse(Browser.FIREFOX == browser);

            assertThat(binaryProvider.providesBinaryForBrowser(browser)).isFalse();
        }

    }

    @Nested
    @DisplayName("when determining the latest binary version")
    class GetLatestBinaryVersionTest {

        @Test
        @DisplayName("should return the latest available version in normalized form")
        void shouldReturnLatestNormalizedVersion() throws IOException {
            final GitHubRelease oldReleaseMock = mock(GitHubRelease.class);

            given(oldReleaseMock.getTagName())
                    .willReturn("v0.18.0");

            given(oldReleaseMock.hasAssetForPlatform(any(Platform.class)))
                    .willReturn(true);

            final GitHubRelease newReleaseMock = mock(GitHubRelease.class);

            given(oldReleaseMock.getTagName())
                    .willReturn("v0.19.1");

            given(oldReleaseMock.hasAssetForPlatform(any(Platform.class)))
                    .willReturn(true);

            given(gitHubReleasesServiceMock.getAllReleases())
                    .willReturn(Arrays.asList(
                            newReleaseMock,
                            oldReleaseMock
                    ));

            assertThat(binaryProvider.getLatestBinaryVersion(Os.WINDOWS, Architecture.X64))
                    .hasValue("0.19.1");
        }

        @Test
        @DisplayName("should return an empty Optional when the desired platform is not supported")
        void shouldReturnEmptyOptionalWhenPlatformIsNotSupported() throws IOException {
            assertThat(binaryProvider.getLatestBinaryVersion(Os.MACOS, Architecture.X86))
                    .isNotPresent();
        }

        @Test
        @DisplayName("should return an empty Optional when no release was found at all")
        void shouldReturnEmptyOptionalWhenNoReleaseExists() throws IOException {
            given(gitHubReleasesServiceMock.getAllReleases())
                    .willReturn(Collections.emptyList());

            assertThat(binaryProvider.getLatestBinaryVersion(Os.LINUX, Architecture.X64))
                    .isNotPresent();
        }

        @Test
        @DisplayName("should return an empty Optional when no release is available for the desired platform")
        void shouldReturnEmptyOptionalWhenPlatformDoesNotMatchAnyRelease() throws IOException {
            final GitHubRelease releaseMock = mock(GitHubRelease.class);

            given(releaseMock.getTagName())
                    .willReturn("doesNotMatter");

            given(releaseMock.hasAssetForPlatform(any(Platform.class)))
                    .willReturn(false);

            given(gitHubReleasesServiceMock.getAllReleases())
                    .willReturn(Collections.singletonList(releaseMock));

            assertThat(binaryProvider.getLatestBinaryVersion(Os.MACOS, Architecture.X64))
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
                    return Browser.FIREFOX == browser;
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
    @DisplayName("should have the same hashCode as the Firefox browser")
    void shouldHaveTheSameHashCodeAsChromeBrowser() {
        assertThat(binaryProvider.hashCode()).isEqualTo(Browser.FIREFOX.hashCode());
    }

}