package sa.urtv.screensaver;

/**
 * Which calculation the prayer times use, for the settings row «طريقة الحساب» and `optimize/saver.sh prayermethod`.
 * «تلقائي» (the default) is the country's own, told by the city's time zone: Saudi Arabia (and every place outside the Gulf, as
 * ever) Umm al-Qura; Kuwait, Qatar, the UAE, Bahrain and Oman each theirs. «جعفري» is the Shia Ithna-Ashari method of the Leva
 * Institute, Qum, for everywhere. The page and the home screen work the same out from the same two things, the zone and
 * {@code pm=jafari} in the settings fragment (CONFIG.prayerMethod in template.html, PrayerMath.methodFor in the launcher);
 * tests/prayer_method_test.py keeps the three tables equal. Plain Java: tests/PrayerMethodRun.java runs it on a JVM.
 */
final class PrayerMethod {
    static final String AUTO = "auto", JAFARI = "jafari";
    /** the countries the city search offers: Saudi Arabia and the other Gulf states (Open-Meteo's country codes) */
    static final String[] SEARCH_COUNTRIES = {"SA", "BH", "KW", "QA", "AE", "OM"};

    private PrayerMethod() { }

    /** "jafari" or "auto"; null when it is neither (a typo from the shell). */
    static String parse(String s) {
        if (s == null) return null;
        String v = s.trim().toLowerCase(java.util.Locale.ROOT);
        return AUTO.equals(v) || JAFARI.equals(v) ? v : null;
    }

    /** What goes in the settings fragment: nothing for «تلقائي» (the page is then as it was). */
    static String fragment(String saved) { return JAFARI.equals(saved) ? "&pm=jafari" : ""; }

    static boolean searchable(String countryCode) {
        for (String c : SEARCH_COUNTRIES) if (c.equalsIgnoreCase(countryCode)) return true;
        return false;
    }

    /** The country's calculation as the settings row names it, by the city's zone. */
    static String authority(String tz) {
        if (tz == null) return "أم القرى";
        switch (tz) {
            case "Asia/Kuwait": return "الكويت";
            case "Asia/Qatar": return "قطر";
            case "Asia/Dubai": return "الإمارات";
            case "Asia/Bahrain": case "Asia/Muscat": return "الخليج";
            default: return "أم القرى";
        }
    }

    static String label(String saved, String tz) {
        return JAFARI.equals(saved) ? "جعفري · معهد ليفا، قم" : "تلقائي · " + authority(tz);
    }
}
