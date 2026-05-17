package org.example.fp;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HughsonWestlakePropertiesTest {
    @Property
    void nextIntensityAlwaysStaysInsideAudiometryRange(
            @ForAll @IntRange(min = 0, max = 26) int step,
            @ForAll PatientResponse response
    ) {
        int validIntensity = HughsonWestlakeAlgorithm.MIN_INTENSITY_DB + (step * 5);

        int nextIntensity = HughsonWestlakeAlgorithm.nextIntensity(validIntensity, response);

        assertTrue(nextIntensity >= HughsonWestlakeAlgorithm.MIN_INTENSITY_DB);
        assertTrue(nextIntensity <= HughsonWestlakeAlgorithm.MAX_INTENSITY_DB);
        assertTrue(nextIntensity % 5 == 0);
    }
}
