package org.example.fp;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.Size;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Property
    void numberOfTrialsAlwaysEqualsNumberOfResponses(
            @ForAll @Size(min = 0, max = 15) List<PatientResponse> responses
    ) {
        AudiometryTestState state = HughsonWestlakeAlgorithm.applyResponses(
                Ear.RIGHT, 1000, responses
        );

        // Eşik bulunmadıysa: tüm cevaplar trial olarak kaydedilmiş olmalı.
        // Eşik bulunduysa: trial sayısı cevap sayısından AZ ya da EŞİT (sonradan gelenler yutuluyor).
        assertTrue(state.trials().size() <= responses.size());
        if (state.threshold().isEmpty()) {
            assertEquals(responses.size(), state.trials().size());
        }
    }

    @Property
    void thresholdOnceFoundNeverChanges(
            @ForAll @Size(min = 5, max = 20) List<PatientResponse> responses,
            @ForAll PatientResponse extra
    ) {
        AudiometryTestState state = HughsonWestlakeAlgorithm.applyResponses(
                Ear.RIGHT, 1000, responses
        );

        // Eşik henüz bulunmadıysa bu testi atla.
        if (state.threshold().isEmpty()) {
            return;
        }

        AudiometryTestState after = HughsonWestlakeAlgorithm.applyResponse(state, extra);
        assertEquals(state.threshold(), after.threshold());
    }

    @Property
    void currentIntensityAlwaysStaysInValidRange(
            @ForAll @Size(min = 0, max = 30) List<PatientResponse> responses,
            @ForAll("validFrequencies") int frequencyHz
    ) {
        AudiometryTestState state = HughsonWestlakeAlgorithm.applyResponses(
                Ear.valueOf("RIGHT"), frequencyHz, responses
        );

        int db = state.currentIntensityDb();
        assertTrue(db >= HughsonWestlakeAlgorithm.MIN_INTENSITY_DB);
        assertTrue(db <= HughsonWestlakeAlgorithm.MAX_INTENSITY_DB);
        assertEquals(0, db % 5);
    }

    @Provide
    Arbitrary<Integer> validFrequencies() {
        return Arbitraries.of(250, 500, 1000, 2000, 4000, 8000);
    }
}
