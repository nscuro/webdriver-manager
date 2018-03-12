package com.github.nscuro.wdm.binary;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;

class BinaryManagerV2ImplTest {

    private Path binaryDestinationFilePathMock;

    private BinaryProvider binaryProviderMock;

    private BinaryManagerV2 binaryManager;

    @BeforeEach
    void beforeEach() {
        binaryDestinationFilePathMock = mock(Path.class);

        binaryProviderMock = mock(BinaryProvider.class);

        binaryManager = new BinaryManagerV2Impl(binaryDestinationFilePathMock, singleton(binaryProviderMock));
    }

}