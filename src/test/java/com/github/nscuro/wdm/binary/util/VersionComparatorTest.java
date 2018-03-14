package com.github.nscuro.wdm.binary.util;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class VersionComparatorTest {

    private Comparator<String> comparator;

    @BeforeEach
    void beforeEach() {
        comparator = new VersionComparator();
    }

    @ParameterizedTest(name = "[{index}]: leftVersion=\"{0}\" rightVersion=\"{1}\"")
    @ValueSource(strings = {
            "0",
            ".",
            "1.0",
            "0.1",
            "0.0.1",
            "0.1.0",
            "0.1.1",
            "1.0.0",
            "1.1.0",
            "1.1.1",
    })
    void shouldReturnZeroWhenComparingEqualVersions(final String version) {
        assertThat(comparator.compare(version, version)).isZero();
    }

    @ParameterizedTest(name = "[{index}]: leftVersion=\"{0}\" rightVersion=\"{1}\"")
    @CsvSource(value = {
            "1, 0",
            "0.1, 0.0",
            "1.0, 0.1",
            "11, 1.0",
            "1.0.0, 0.0.1",
            "0.1.0, 0.0.1",
            "0.1.1, 0.0.1",
            "0.11.1, 0.1.0"
            // TODO: Add more values
    })
    void shouldCorrectlyIndicateWhichVersionIsHigher(final String leftVersion, final String rightVersion) {
        final SoftAssertions assertions = new SoftAssertions();

        assertions
                .assertThat(comparator.compare(leftVersion, rightVersion))
                .as("should return greater than zero when leftVersion is higher than rightVersion")
                .isGreaterThan(0);

        assertions
                .assertThat(comparator.compare(rightVersion, leftVersion))
                .as("should return less than zero when leftVersion is lower than rightVersion")
                .isLessThan(0);

        assertions.assertAll();
    }

    @Test
    void shouldThrowExceptionWhenInputsAreNotVersionStrings() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> comparator.compare("not.a.version", "1.0"));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> comparator.compare("1.0", "not.a.version"));
    }

}