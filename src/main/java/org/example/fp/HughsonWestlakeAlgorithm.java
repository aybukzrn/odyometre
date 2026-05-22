package org.example.fp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class HughsonWestlakeAlgorithm {
    public static final List<Integer> TEST_FREQUENCIES = List.of(250, 500, 1000, 2000, 4000, 8000);
    public static final int MIN_INTENSITY_DB = -10;
    public static final int MAX_INTENSITY_DB = 120;
    public static final int START_INTENSITY_DB = 40;
    public static final int ASCENDING_RESPONSES_REQUIRED = 2;

    private HughsonWestlakeAlgorithm() {
    }

    public static AudiometryTestState initialState(Ear ear, int frequencyHz) {
        return new AudiometryTestState(ear, frequencyHz, START_INTENSITY_DB, List.of(), Optional.empty());
    }

    public static AudiometryTestState initialState(Ear ear, int frequencyHz, int startIntensityDb) {
        return new AudiometryTestState(ear, frequencyHz, startIntensityDb, List.of(), Optional.empty());
    }

    public static AudiometryTestState applyResponse(AudiometryTestState state, PatientResponse response) {
        if (state.isCompleted()) {
            return state;
        }
        if (response == null) {
            throw new IllegalArgumentException("response must not be null");
        }

        // Bu sunum "ascending" mi? Bir önceki yanıt NOT_HEARD ise seviye +5 ile YUKARI çıkılarak
        // bu seviyeye gelinmiştir; klinik olarak ascending sunum budur. İlk sunum ve HEARD'den
        // sonra inilen sunumlar descending sayılır (eşik sayımına dahil edilmez).
        boolean ascending = !state.trials().isEmpty()
                && state.trials().get(state.trials().size() - 1).response() == PatientResponse.NOT_HEARD;

        Trial newTrial = new Trial(state.frequencyHz(), state.currentIntensityDb(), response, ascending);
        List<Trial> updatedTrials = append(state.trials(), newTrial);
        Optional<ThresholdResult> threshold = detectThreshold(state.ear(), state.frequencyHz(), updatedTrials);
        int nextIntensity = threshold.isPresent()
                ? state.currentIntensityDb()
                : nextIntensity(state.currentIntensityDb(), response);

        return new AudiometryTestState(state.ear(), state.frequencyHz(), nextIntensity, updatedTrials, threshold);
    }

    public static AudiometryTestState applyResponses(
            Ear ear,
            int frequencyHz,
            Collection<PatientResponse> responses
    ) {
        return responses.stream()
                .reduce(
                        initialState(ear, frequencyHz),
                        HughsonWestlakeAlgorithm::applyResponse,
                        (left, right) -> right
                );
    }

    public static int nextIntensity(int currentIntensityDb, PatientResponse response) {
        validateIntensity(currentIntensityDb);
        return switch (response) {
            case HEARD -> clampToValidIntensity(currentIntensityDb - 10);
            case NOT_HEARD -> clampToValidIntensity(currentIntensityDb + 5);
        };
    }

    public static Optional<ThresholdResult> detectThreshold(Ear ear, int frequencyHz, List<Trial> trials) {
        validateFrequency(frequencyHz);

        // IEC 60645-1 / ISO 8253-1: eşik, aynı seviyede en az 2 (3 sunumdan) ASCENDING
        // yanıtın HEARD olduğu en düşük şiddet seviyesidir. Descending yanıtlar sayılmaz.
        Map<Integer, Long> ascendingHeardByIntensity = trials.stream()
                .filter(trial -> trial.frequencyHz() == frequencyHz)
                .filter(Trial::ascending)
                .filter(trial -> trial.response() == PatientResponse.HEARD)
                .collect(Collectors.groupingBy(Trial::intensityDb, Collectors.counting()));

        return ascendingHeardByIntensity.entrySet().stream()
                .filter(entry -> entry.getValue() >= ASCENDING_RESPONSES_REQUIRED)
                .map(Map.Entry::getKey)
                .min(Comparator.naturalOrder())
                .map(thresholdDb -> new ThresholdResult(ear, frequencyHz, thresholdDb, evidenceFor(trials, frequencyHz, thresholdDb)));
    }

    public static List<ThresholdResult> completedThresholds(Collection<AudiometryTestState> states) {
        return states.stream()
                .map(AudiometryTestState::threshold)
                .flatMap(Optional::stream)
                .toList();
    }

    public static void validateFrequency(int frequencyHz) {
        if (!TEST_FREQUENCIES.contains(frequencyHz)) {
            throw new IllegalArgumentException("Unsupported audiometry frequency: " + frequencyHz);
        }
    }

    public static void validateIntensity(int intensityDb) {
        if (intensityDb < MIN_INTENSITY_DB || intensityDb > MAX_INTENSITY_DB || intensityDb % 5 != 0) {
            throw new IllegalArgumentException("Unsupported audiometry intensity: " + intensityDb);
        }
    }

    private static int clampToValidIntensity(int intensityDb) {
        return Math.max(MIN_INTENSITY_DB, Math.min(MAX_INTENSITY_DB, intensityDb));
    }

    private static List<Trial> append(List<Trial> trials, Trial newTrial) {
        List<Trial> updatedTrials = new ArrayList<>(trials);
        updatedTrials.add(newTrial);
        return List.copyOf(updatedTrials);
    }

    private static List<Trial> evidenceFor(List<Trial> trials, int frequencyHz, int thresholdDb) {
        return trials.stream()
                .filter(trial -> trial.frequencyHz() == frequencyHz)
                .filter(trial -> trial.intensityDb() == thresholdDb)
                .filter(Trial::ascending)
                .filter(trial -> trial.response() == PatientResponse.HEARD)
                .toList();
    }
}