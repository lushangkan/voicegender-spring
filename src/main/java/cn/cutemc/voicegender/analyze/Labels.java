package cn.cutemc.voicegender.analyze;

public enum Labels {

    MASCULINE,
    FEMININE;

    public static Labels getLabel(String label) {
        for (Labels value : values()) {
            if (value.name().equalsIgnoreCase(label)) {
                return value;
            }
        }
        return null;
    }
}
