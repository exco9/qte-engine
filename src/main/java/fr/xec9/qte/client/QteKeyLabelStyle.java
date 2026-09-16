package fr.xec9.qte.client;

final class QteKeyLabelStyle {
    private static final int[] SMALL_CAPS = QteHudModel.smallCaps("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        .codePoints()
        .toArray();

    private QteKeyLabelStyle() {}

    static String forFont(String label, QteClientConfig.KeyFont font) {
        return font == QteClientConfig.KeyFont.MINECRAFT_FIVE ? regularCaps(label) : label;
    }

    static String regularCaps(String value) {
        StringBuilder result = new StringBuilder(value.length());
        value.codePoints().forEach(codePoint -> {
            int index = smallCapIndex(codePoint);
            result.appendCodePoint(index >= 0 ? 'A' + index : codePoint);
        });
        return result.toString();
    }

    private static int smallCapIndex(int codePoint) {
        for (int index = 0; index < SMALL_CAPS.length; index++) {
            if (SMALL_CAPS[index] == codePoint) {
                return index;
            }
        }
        return -1;
    }
}
