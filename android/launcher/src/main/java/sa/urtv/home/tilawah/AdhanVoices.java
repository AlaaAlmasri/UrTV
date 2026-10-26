package sa.urtv.home.tilawah;

/**
 * Which of the 21 recordings are for Fajr and which for the other prayers, the short adhan's fade, and the rule for a TV that was set up
 * before «مؤذن الفجر» existed. No Android in it (tests/AdhanVoicesRun.java runs it).
 *
 * Four recordings are Fajr adhans (with «الصلاة خير من النوم»): they play at Fajr only and are not in the list for the other prayers.
 * The Fajr list has them first and then the normal ones.
 */
final class AdhanVoices {
    static final int COUNT = 21;
    /** the voice a TV starts with for the five prayers that are not Fajr, and for Fajr («فجر الحرم المكي») */
    static final int DEFAULT = 1, DEFAULT_FAJR = 10;
    /** the Fajr recordings: فجر الحرم المكي, أذان القاهرة (Cairo AlFajr), أذان أم القيوين (AlFajr Umm alQaiwain), أذان ماليزيا (Malaysia Al Fajr) */
    private static final int[] FAJR = {10, 13, 17, 20};

    /** «أذان مختصر»: the adhan stops after this long, the last FADE_MS of it a fade to silence */
    static final long SHORT_MS = 60_000, FADE_MS = 3_000;

    private AdhanVoices() { }

    static boolean valid(int voice) { return voice >= 1 && voice <= COUNT; }

    static boolean isFajr(int voice) {
        for (int v : FAJR) if (v == voice) return true;
        return false;
    }

    /** the recordings for the five other prayers, in their order */
    static int[] normal() {
        int[] out = new int[COUNT - FAJR.length];
        int k = 0;
        for (int v = 1; v <= COUNT; v++) if (!isFajr(v)) out[k++] = v;
        return out;
    }

    /** the recordings to pick from for Fajr: the Fajr ones, then the normal ones */
    static int[] fajrList() {
        int[] out = new int[COUNT];
        int k = 0;
        for (int v : FAJR) out[k++] = v;
        for (int v : normal()) out[k++] = v;
        return out;
    }

    /** the saved voice for the other prayers: a Fajr recording (kept by a TV that chose one before) or nonsense is the default */
    static int normalVoice(int saved) { return valid(saved) && !isFajr(saved) ? saved : DEFAULT; }

    /**
     * The voice for Fajr. savedFajr is what was chosen for Fajr (-1: never); savedNormal is the old single voice. A TV whose one voice was a
     * Fajr recording keeps it for Fajr; every other TV starts with the default.
     */
    static int fajrVoice(int savedFajr, int savedNormal) {
        if (valid(savedFajr)) return savedFajr;
        return valid(savedNormal) && isFajr(savedNormal) ? savedNormal : DEFAULT_FAJR;
    }

    /** the player's volume 0..1 at this moment of a short adhan: full, then down to nothing over the last FADE_MS */
    static float shortGain(long elapsedMs) {
        if (elapsedMs >= SHORT_MS) return 0f;
        long fadeStart = SHORT_MS - FADE_MS;
        if (elapsedMs <= fadeStart) return 1f;
        return (SHORT_MS - elapsedMs) / (float) FADE_MS;
    }
}
