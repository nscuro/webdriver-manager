package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.AbstractBinaryProviderIT;
import com.github.nscuro.wdm.binary.BinaryProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ChromeDriverBinaryProviderIT extends AbstractBinaryProviderIT {

    private static final String VERSION_REGEX = "[0-9]+.[0-9]+";

    private BinaryProvider binaryProvider;

    @Override
    @BeforeEach
    protected void beforeEach() {
        super.beforeEach();

        binaryProvider = new ChromeDriverBinaryProvider(httpClient);
    }

    @ParameterizedTest
    @MethodSource("provideSupportedPlatforms")
    void shouldReturnLatestVersionForSupportedPlatforms(final Os os, final Architecture architecture) throws IOException {
        assertThat(binaryProvider.getLatestBinaryVersion(os, architecture))
                .get().asString().matches(VERSION_REGEX);
    }

    @Test
    void shouldReturnLatestVersionForDiscontinuedLinux32Platform() throws IOException {
        assertThat(binaryProvider.getLatestBinaryVersion(Os.LINUX, Architecture.X86))
                .contains("2.33");
    }

    @ParameterizedTest
    @MethodSource("provideBinaryVersionForSupportedPlatforms")
    void shouldBeAbleToDownloadSpecificVersionForSupportedPlatforms(final String version, final Os os, final Architecture architecture) throws IOException {
        downloadedFile = binaryProvider.download(version, os, architecture, getBinaryDestinationPath(getClass()));

        assertThat(downloadedFile).exists();
    }

    private static Stream<Arguments> provideSupportedPlatforms() {
        return Stream.of(
                Arguments.of(Os.WINDOWS, Architecture.X86),
                Arguments.of(Os.WINDOWS, Architecture.X64),
                Arguments.of(Os.LINUX, Architecture.X86),
                Arguments.of(Os.LINUX, Architecture.X64),
                Arguments.of(Os.MACOS, Architecture.X64)
        );
    }

    private static Stream<Arguments> provideBinaryVersionForSupportedPlatforms() {
        return Stream.of(
                Arguments.of("2.35", Os.WINDOWS, Architecture.X86),
                Arguments.of("2.35", Os.WINDOWS, Architecture.X64),
                Arguments.of("2.33", Os.LINUX, Architecture.X86),
                Arguments.of("2.35", Os.LINUX, Architecture.X64),
                Arguments.of("2.35", Os.MACOS, Architecture.X64)
        );
    }

}