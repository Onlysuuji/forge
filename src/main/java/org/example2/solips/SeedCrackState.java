package org.example2.solips;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SeedCrackState {
    private static volatile boolean running = false;
    private static volatile boolean solved = false;
    private static volatile int solvedSeed = 0;
    private static volatile int checked = 0;
    private static volatile int matched = 0;
    private static volatile String lastObservationKey = "";
    private static final List<Integer> candidates = Collections.synchronizedList(new ArrayList<>());

    private SeedCrackState() {}

    public static void reset() {
        running = false;
        solved = false;
        solvedSeed = 0;
        checked = 0;
        matched = 0;
        candidates.clear();
    }

    public static void start() {
        reset();
        running = true;
    }

    public static void finish() {
        running = false;
        if (candidates.size() == 1) {
            solved = true;
            solvedSeed = candidates.get(0);
        }
    }

    public static boolean isRunning() { return running; }
    public static boolean isSolved() { return solved; }
    public static int getSolvedSeed() { return solvedSeed; }
    public static int getChecked() { return checked; }
    public static int getMatched() { return matched; }
    public static List<Integer> getCandidatesSnapshot() { return new ArrayList<>(candidates); }

    public static void setChecked(int value) { checked = value; }

    public static void addCandidate(int seed) {
        candidates.add(seed);
        matched = candidates.size();
    }

    public static String getLastObservationKey() {
        return lastObservationKey;
    }

    public static void setLastObservationKey(String key) {
        lastObservationKey = key;
    }
}