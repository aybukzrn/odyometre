package org.example.fp;

import java.util.List;

public record ThresholdResult(Ear ear, int frequencyHz, int thresholdDb, List<Trial> evidence) {
    public ThresholdResult {
        if (ear == null) {
            throw new IllegalArgumentException("ear must not be null");
        }
        HughsonWestlakeAlgorithm.validateFrequency(frequencyHz);
        HughsonWestlakeAlgorithm.validateIntensity(thresholdDb);
        evidence = List.copyOf(evidence);
        if (evidence.isEmpty()) {
            throw new IllegalArgumentException("evidence must not be empty");
        }
    }
}
