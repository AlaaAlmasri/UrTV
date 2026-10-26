package sa.urtv.screensaver;

/** For tests/versions_test.py: Versions.java on a plain JVM. Prints PASS/FAIL lines. */
public final class VersionsRun {
    static int failed;

    static void check(String name, boolean ok, String detail) {
        System.out.println((ok ? "PASS " : "FAIL ") + name + (ok ? "" : "  -> " + detail));
        if (!ok) failed++;
    }

    static boolean newer(String a, String b) { Integer c = Versions.compare(a, b); return c != null && c > 0; }

    public static void main(String[] args) {
        check("3.12 is newer than 3.9 (a number, not text)", newer("3.12", "3.9"), "");
        check("4.0 is newer than 3.12 (major first)", newer("4.0", "3.12"), "");
        check("4.1 is newer than 3.99", newer("4.1", "3.99"), "");
        check("3.9 is not newer than 4.0", !newer("3.9", "4.0"), "");
        check("equal versions are not newer", Versions.compare("3.12", "3.12") == 0, "");
        check("3.5 is older than 3.12", Versions.compare("3.5", "3.12") < 0, "");
        check("a bare 4 is 4.0", Versions.compare("4", "4.0") == 0 && newer("4", "3.30"), "");
        check("what does not parse is null", Versions.parse("abc") == null && Versions.parse(null) == null && Versions.parse("") == null
                && Versions.parse("3.x") == null && Versions.parse("-1.2") == null && Versions.compare("x", "3.1") == null, "");
        check("version.txt: 3.51 with its newline", "3.51".equals(Versions.fromText("3.51\n")), "");
        check("version.txt: a page or an error is not a version", Versions.fromText("<html>404</html>") == null && Versions.fromText("") == null
                && Versions.fromText(null) == null && Versions.fromText("3.51 extra") == null && Versions.fromText("Not Found") == null, "");
        check("redirect to the numbered tag", "3.51".equals(Versions.fromTag("https://github.com/tshi-dev/UrTV/releases/tag/v3.51")), "");
        check("redirect to apk-latest (what GitHub gives today) is not a version", Versions.fromTag("https://github.com/tshi-dev/UrTV/releases/tag/apk-latest") == null
                && Versions.fromTag("https://github.com/tshi-dev/UrTV/releases/tag/apk-next") == null && Versions.fromTag(null) == null, "");
        check("3.100 from the file is newer than 3.99", newer(Versions.fromText("3.100"), "3.99"), "");
        System.exit(failed == 0 ? 0 : 1);
    }
}
