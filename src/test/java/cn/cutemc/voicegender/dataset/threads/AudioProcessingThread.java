package cn.cutemc.voicegender.dataset.threads;

import cn.cutemc.voicegender.analyze.FFmpegService;
import cn.cutemc.voicegender.analyze.Features;
import cn.cutemc.voicegender.utils.AnalyzeUtils;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class AudioProcessingThread extends Thread {

    private File audioFile;

    private Path tmpPath;

    private String gender;

    private List<String[]> table;

    private List<Thread> threads;

    public AudioProcessingThread setAudioFile(File audioFile) {
        this.audioFile = audioFile;
        return this;
    }

    public AudioProcessingThread setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public AudioProcessingThread setTable(List<String[]> table) {
        this.table = table;
        return this;
    }

    public AudioProcessingThread setTmpPath(Path tmpPath) {
        this.tmpPath = tmpPath;
        return this;
    }

    public AudioProcessingThread setThreads(List<Thread> threads) {
        this.threads = threads;
        return this;
    }

    @Override
    public void run() {

        super.run();

        File audio = audioFile;

        Map<Features, Double> features;

        File newAudio = new File(tmpPath.toFile(), audio.getName().replaceAll("(\\.[^.]+)$", ".wav"));

        long startTime = System.currentTimeMillis();

        //Watchdog
        Thread watchdog = new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    if (System.currentTimeMillis() - startTime > 1 * 60 * 1000) {
                        System.out.println("超时：" + newAudio.getName());
                        threads.remove(AudioProcessingThread.this);
                        newAudio.delete();
                        AudioProcessingThread.this.interrupt();
                        this.interrupt();
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        try {

            //检查时长
            //if (FFmpegUtils.getDuration(audio) < 5) {
            //    System.out.println("时长不足：" + audio.getName());
            //    threads.remove(this);
           //     this.stop();
            //    return;
            //}

            //音频预处理
            try {
                new FFmpegService().audioPreprocessing(audio.toPath(), newAudio.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while (!newAudio.exists()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            audio = newAudio;

            try {
                features = AnalyzeUtils.ComputedFeatures(audio.toPath());
            } catch (IOException | ScriptException e) {
                throw new RuntimeException(e);
            }

            String[] row = new String[]{
                    String.valueOf(features.get(Features.MEAN_FREQ)),
                    String.valueOf(features.get(Features.SD)),
                    String.valueOf(features.get(Features.MEDIAN)),
                    String.valueOf(features.get(Features.Q25)),
                    String.valueOf(features.get(Features.Q75)),
                    String.valueOf(features.get(Features.IQR)),
                    String.valueOf(features.get(Features.SKEW)),
                    String.valueOf(features.get(Features.KURT)),
                    String.valueOf(features.get(Features.SP_ENT)),
                    String.valueOf(features.get(Features.SFM)),
                    String.valueOf(features.get(Features.MODE)),
                    String.valueOf(features.get(Features.CENTROID)),
                    String.valueOf(features.get(Features.MEAN_FUN)),
                    String.valueOf(features.get(Features.MIN_FUN)),
                    String.valueOf(features.get(Features.MAX_FUN)),
                    String.valueOf(features.get(Features.MEAN_DOM)),
                    String.valueOf(features.get(Features.MIN_DOM)),
                    String.valueOf(features.get(Features.MAX_DOM)),
                    String.valueOf(features.get(Features.DF_RANGE)),
                    String.valueOf(features.get(Features.MODINDX)),
                    gender
            };

            table.add(row);

            System.out.println(row[0] + " " + row[1] + " " + row[2] + " " + row[3] + " " + row[4] + " " + row[5] + " " + row[6] + " " + row[7] + " " + row[8] + " " + row[9] + " " + row[10] + " " + row[11] + " " + row[12] + " " + row[13] + " " + row[14] + " " + row[15] + " " + row[16] + " " + row[17] + " " + row[18] + " " + row[19] + " " + row[20]);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.threads.remove(this);

            newAudio.delete();

            this.interrupt();
        }
    }
}
