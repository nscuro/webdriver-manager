package com.github.nscuro.wdm.binary.util;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @since 0.2.0
 */
public final class VersionComparator implements Comparator<String> {

    private static final String VERSION_REGEX = "[0-9|.]+";

    @Override
    public int compare(final String v1, final String v2) {
        final Iterator<Integer> v1VersionsIterator = parseVersions(v1);

        final Iterator<Integer> v2VersionsIterator = parseVersions(v2);

        while (v1VersionsIterator.hasNext() && v2VersionsIterator.hasNext()) {
            final int comparisonResult = Integer.compare(v1VersionsIterator.next(), v2VersionsIterator.next());

            if (comparisonResult != 0) {
                return comparisonResult;
            }
        }

        return 0;
    }

    @Nonnull
    private Iterator<Integer> parseVersions(final String versionString) {
        if (!requireNonNull(versionString).matches(VERSION_REGEX)) {
            throw new IllegalArgumentException(
                    format("\"%s\" is not a version string (must match \"%s\")", versionString, VERSION_REGEX));
        }

        return Arrays
                .stream(versionString.split("\\."))
                .map(Integer::parseInt)
                .iterator();
    }

}
