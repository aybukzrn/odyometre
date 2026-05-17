package org.example.fp;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class AudiometryTestStateTest {

    @Test
    void trialsListIsDefensivelyCopiedAndImmutable() {
        // Dışarıdan verilen mutable liste sonradan değiştirilse bile state'in trial'ları etkilenmemeli.
        List<Trial> mutableTrials = new ArrayList<>();
        mutableTrials.add(new Trial(1000, 40, PatientResponse.HEARD));

        AudiometryTestState state = new AudiometryTestState(
                Ear.RIGHT, 1000, 40, mutableTrials, Optional.empty()
        );

        // Orijinal listeyi değiştir.
        mutableTrials.add(new Trial(1000, 30, PatientResponse.HEARD));

        // State içindeki trials etkilenmemeli.
        assertEquals(1, state.trials().size());

        // State'in döndürdüğü liste de modify edilemez olmalı.
        assertThrows(
                UnsupportedOperationException.class,
                () -> state.trials().add(new Trial(1000, 20, PatientResponse.NOT_HEARD))
        );
    }

    @Test
    void nullEarIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AudiometryTestState(null, 1000, 40, List.of(), Optional.empty())
        );
    }

    @Test
    void nullThresholdOptionalIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AudiometryTestState(Ear.RIGHT, 1000, 40, List.of(), null)
        );
    }

    @Test
    void applyResponseProducesDistinctStateInstance() {
        AudiometryTestState before = HughsonWestlakeAlgorithm.initialState(Ear.LEFT, 2000);
        AudiometryTestState after = HughsonWestlakeAlgorithm.applyResponse(before, PatientResponse.HEARD);

        assertNotSame(before, after);
        assertEquals(0, before.trials().size());
        assertEquals(1, after.trials().size());
    }
}