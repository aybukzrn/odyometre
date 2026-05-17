package org.example.fp;

import java.util.List;
import java.util.Optional;

public record AudiometryTestState(
        Ear ear,
        int frequencyHz,
        int currentIntensityDb,
        List<Trial> trials,
        Optional<ThresholdResult> threshold
) {
    public AudiometryTestState {
        if (ear == null) {
            throw new IllegalArgumentException("ear must not be null");
        }
        HughsonWestlakeAlgorithm.validateFrequency(frequencyHz);
        HughsonWestlakeAlgorithm.validateIntensity(currentIntensityDb);
        trials = List.copyOf(trials);
        if (threshold == null) {
            throw new IllegalArgumentException("threshold must not be null");
        }
    }

    public boolean isCompleted() {
        return threshold.isPresent();
    }
}
