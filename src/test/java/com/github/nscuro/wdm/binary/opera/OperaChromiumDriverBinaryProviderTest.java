package com.github.nscuro.wdm.binary.opera;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.Platform;
import com.github.nscuro.wdm.binary.util.compression.BinaryExtractorFactory;
import com.github.nscuro.wdm.binary.util.github.GitHubRelease;
import com.github.nscuro.wdm.binary.util.github.GitHubReleasesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
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
        @EnumSource(Browser.class)
        void shouldReturnFalseForEveryBrowserExceptOpera(final Browser browser) {
            assumeFalse(Browser.OPERA == browser);

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

}