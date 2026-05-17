package org.example.fp;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HughsonWestlakeAlgorithmTest {
    @Test
    void heardResponseDecreasesIntensityByTenDb() {
        int nextIntensity = HughsonWestlakeAlgorithm.nextIntensity(40, PatientResponse.HEARD);

        assertEquals(30, nextIntensity);
    }

    @Test
    void initialStateCanUseGuiSelectedStartingIntensity() {
        AudiometryTestState state = HughsonWestlakeAlgorithm.initialState(Ear.RIGHT, 1000, 55);

        assertEquals(55, state.currentIntensityDb());
    }

    @Test
    void notHeardResponseIncreasesIntensityByFiveDb() {
        int nextIntensity = HughsonWestlakeAlgorithm.nextIntensity(40, PatientResponse.NOT_HEARD);

        assertEquals(45, nextIntensity);
    }

    @Test
    void applyingResponseCreatesNewImmutableState() {
        AudiometryTestState initial = HughsonWestlakeAlgorithm.initialState(Ear.RIGHT, 1000);

        AudiometryTestState updated = HughsonWestlakeAlgorithm.applyResponse(initial, PatientResponse.HEARD);

        assertNotSame(initial, updated);
        assertTrue(initial.trials().isEmpty());
        assertEquals(List.of(new Trial(1000, 40, PatientResponse.HEARD)), updated.trials());
    }

    @Test
    void thresholdIsDetectedAfterTwoHeardResponsesAtSameLowestIntensity() {
        AudiometryTestState state = HughsonWestlakeAlgorithm.applyResponses(
                Ear.LEFT,
                1000,
                List.of(
                        PatientResponse.HEARD,
                        PatientResponse.HEARD,
                        PatientResponse.NOT_HEARD,
                        PatientResponse.NOT_HEARD,
                        PatientResponse.HEARD
                )
        );

        assertTrue(state.threshold().isPresent());
        assertEquals(30, state.threshold().orElseThrow().thresholdDb());
    }

    @Test
    void completedThresholdsUsesOptionalPipeline() {
        AudiometryTestState incomplete = HughsonWestlakeAlgorithm.initialState(Ear.RIGHT, 500);
        AudiometryTestState complete = HughsonWestlakeAlgorithm.applyResponses(
                Ear.RIGHT,
                500,
                List.of(
                        PatientResponse.HEARD,
                        PatientResponse.HEARD,
                        PatientResponse.NOT_HEARD,
                        PatientResponse.NOT_HEARD,
                        PatientResponse.HEARD
                )
        );

        List<ThresholdResult> thresholds = HughsonWestlakeAlgorithm.completedThresholds(List.of(incomplete, complete));

        assertEquals(1, thresholds.size());
        assertEquals(500, thresholds.get(0).frequencyHz());
    }

    @Test
    void invalidFrequencyIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> HughsonWestlakeAlgorithm.initialState(Ear.RIGHT, 125)
        );
    }
}
