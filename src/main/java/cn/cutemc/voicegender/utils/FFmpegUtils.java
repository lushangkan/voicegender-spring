package cn.cutemc.voicegender.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class FFmpegUtils {

    /**
     * 获取 FFmpeg 路径
     * @return FFmpeg 路径集合
     */
    public static ArrayList<String> getFFmpegPath() {
        ArrayList<String> FFmpegPaths = new ArrayList<>();

        String path = System.getenv("PATH");
        String[] paths = path.split(System.getProperty("path.separator"));
        for (String p : paths) {
            File pathFile = new File(p);
            if (!pathFile.isDirectory()) continue;
            File[] files = Objects.requireNonNull(pathFile.listFiles());
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith("ffmpeg") || file.getName().endsWith("ffmpeg.exe")) {
                    FFmpegPaths.add(file.getAbsolutePath());
                }
            }
        }

        return FFmpegPaths;
    }

}
