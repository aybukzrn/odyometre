package org.example.fp;

public record Trial(int frequencyHz, int intensityDb, PatientResponse response) {
    public Trial {
        HughsonWestlakeAlgorithm.validateFrequency(frequencyHz);
        HughsonWestlakeAlgorithm.validateIntensity(intensityDb);
        if (response == null) {
            throw new IllegalArgumentException("response must not be null");
        }
    }
}
