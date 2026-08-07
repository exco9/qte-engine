package fr.xec9.qte.domain;

public final class KeyboardLayoutDetector {
    private KeyboardLayoutDetector() {}

    public static String detect(String physicalQ, String physicalW, String physicalY) {
        if (equalsKey(physicalQ, "a") && equalsKey(physicalW, "z")) {
            return "AZERTY";
        }
        if (equalsKey(physicalY, "z")) {
            return "QWERTZ";
        }
        if (equalsKey(physicalQ, "q") && equalsKey(physicalW, "w") && equalsKey(physicalY, "y")) {
            return "QWERTY";
        }
        return "CUSTOM";
    }

    private static boolean equalsKey(String actual, String expected) {
        return actual != null && actual.equalsIgnoreCase(expected);
    }
}
