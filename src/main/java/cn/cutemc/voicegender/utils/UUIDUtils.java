package cn.cutemc.voicegender.utils;

import java.util.List;
import java.util.UUID;

public class UUIDUtils {

    /**
     * 随机生成UUID
     * @return UUID
     */
    public static UUID randomUUID() {
        return UUID.randomUUID();
    }

    public static boolean isUUID(String uuid) {
        return uuid.matches("[0-9a-f]{8}(-[0-9a-f]{4}){4}[0-9a-f]{8}");
    }

    public static UUID getFormPath(String path) {
        List<String> list =  RegexUtils.getMatchList(path, "(?<=analyze/).*?(?=/)", false);
        for (String s : list) {
            if (isUUID(s)) {
                return UUID.fromString(s);
            }
        }

        return null;
    }

    public static String uuidToString(UUID uuid) {
        if (uuid == null) {
            return "null";
        }
        return uuid.toString();
    }
}
