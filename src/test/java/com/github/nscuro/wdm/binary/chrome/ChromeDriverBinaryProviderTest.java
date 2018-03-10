package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageDirectory;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageEntry;
import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.github.nscuro.wdm.binary.chrome.ChromeDriverPlatform.WIN32;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ChromeDriverBinaryProviderTest {

    private HttpClient httpClientMock;

    private GoogleCloudStorageDirectory cloudStorageDirectoryMock;

    private ChromeDriverBinaryProvider binaryProvider;

    @BeforeEach
    void beforeEach() {
        httpClientMock = mock(HttpClient.class);

        cloudStorageDirectoryMock = mock(GoogleCloudStorageDirectory.class);

        binaryProvider = new ChromeDriverBinaryProvider(httpClientMock, cloudStorageDirectoryMock);
    }

    @Nested
    class GetLatestBinaryVersionTest {

        @Test
        void shouldReturnLatestAvailableVersion() throws IOException {
            given(cloudStorageDirectoryMock.getEntries())
                    .willReturn(Arrays.asList(
                            new GoogleCloudStorageEntry(format("5.6/%s", WIN32), null, null),
                            new GoogleCloudStorageEntry(format("1.11/%s", WIN32), null, null)
                    ));

            assertThat(binaryProvider.getLatestBinaryVersion(Os.WINDOWS, Architecture.X64))
                    .contains("5.6");

            assertThat(binaryProvider.getLatestBinaryVersion(Os.WINDOWS, Architecture.X86))
                    .contains("5.6");
        }

        @Test
        void shouldReturnEmptyOptionalWhenNoEntriesHaveBeenFound() throws IOException {
            given(cloudStorageDirectoryMock.getEntries())
                    .willReturn(emptyList());

            assertThat(binaryProvider.getLatestBinaryVersion(Os.WINDOWS, Architecture.X64))
                    .isEmpty();
        }

        @Test
        void shouldReturnEmptyOptionalWhenPlatformDoesNotMatch() throws IOException {
            given(cloudStorageDirectoryMock.getEntries())
                    .willReturn(singletonList(new GoogleCloudStorageEntry(format("2.2/%s", WIN32), null, null)));

            assertThat(binaryProvider.getLatestBinaryVersion(Os.LINUX, Architecture.X64))
                    .isEmpty();
        }

    }

}