package com.github.nscuro.wdm.binary;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;

class BinaryManagerImplTest {

    private Path binaryDestinationFilePathMock;

    private BinaryProvider binaryProviderMock;

    private BinaryManager binaryManager;

    @BeforeEach
    void beforeEach() {
        binaryDestinationFilePathMock = mock(Path.class);

        binaryProviderMock = mock(BinaryProvider.class);

        binaryManager = new BinaryManagerImpl(binaryDestinationFilePathMock, singleton(binaryProviderMock));
    }

}