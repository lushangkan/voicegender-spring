package cn.cutemc.voicegender.dataset;

import cn.cutemc.voicegender.analyze.FFmpegService;
import cn.cutemc.voicegender.analyze.Features;
import cn.cutemc.voicegender.dataset.threads.AudioProcessingThread;
import cn.cutemc.voicegender.utils.AnalyzeUtils;
import cn.cutemc.voicegender.utils.CsvUtils;
import cn.cutemc.voicegender.utils.DataSetUtils;
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrainingSetsCreator {

    @Test
    public void generate() throws IOException, CsvException, ScriptException, InterruptedException {

        //数据集目录
        Path dataSetPath = Path.of("F:\\voice_dataset\\ST-CMDS-20170001_1-OS");

        //临时文件夹
        Path tmpPath = Path.of("C:\\Users\\11490\\OneDrive\\java\\voicegender\\tmp\\");

        //删除tmpPath里的文件
        File[] files = tmpPath.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }

        //读取数据集
        Map<String, String> csvMap = DataSetUtils.getDataSet(dataSetPath, "dataset.csv");

        //随机取100个
        csvMap = DataSetUtils.sample(csvMap, 800);

        //新建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(40);
        List<Thread> threads = new ArrayList<>();

        //新建表格
        List<String[]> table = new ArrayList<>();

        //设置表格头
        table.add(new String[]{"meanfreq", "sd", "median", "Q25", "Q75", "IQR", "skew", "kurt", "sp.ent", "sfm", "models", "centroid", "meanfun", "minfun", "maxfun", "meandom", "mindom", "maxdom", "dfrange", "modindx", "label"});

        //保存CSV
        Thread tableSaver = new Thread(() -> {
            while (true) {
                try {
                    CsvUtils.saveCsv(Path.of("C:\\Users\\11490\\OneDrive\\java\\voicegender\\output.csv"), table);
                } catch (IOException | CsvException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        tableSaver.start();

        SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");

        for (Map.Entry<String, String> entry : csvMap.entrySet()) {
            Path path = Path.of(dataSetPath.toString(), entry.getKey());
            String gender = entry.getValue();

            Thread thread = new AudioProcessingThread()
                    .setTmpPath(tmpPath)
                    .setAudioFile(path.toFile())
                    .setGender(gender)
                    .setTable(table)
                    .setThreads(threads);

            threads.add(thread);

            executorService.submit(thread);
        }

        long startTime = System.currentTimeMillis();

        //设置剩余输出
        while (threads.size() > 0) {
            System.out.println("剩余：" + threads.size() + " 时间: " + ft.format(new Date(System.currentTimeMillis() - startTime)));
            Thread.sleep(1000);
        }

        CsvUtils.saveCsv(Path.of("C:\\Users\\11490\\OneDrive\\java\\voicegender\\output.csv"), table);
    }


    @Test
    public void generateOne() throws IOException, CsvException {

        Path tmpPath = Path.of("C:\\Users\\11490\\OneDrive\\java\\voicegender\\tmp\\");

        File audio = new File("F:\\voice-gender\\1.wav");

        String gender = "female";

        //删除tmpPath里的文件
        File[] files = tmpPath.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }

        List<String[]> table = new ArrayList<>();

        table.add(new String[]{"meanfreq", "sd", "median", "Q25", "Q75", "IQR", "skew", "kurt", "sp.ent", "sfm", "models", "centroid", "meanfun", "minfun", "maxfun", "meandom", "mindom", "maxdom", "dfrange", "modindx", "label"});

        Map<Features, Double> features;

        File newAudio = new File(tmpPath.toFile(), audio.getName().replaceAll("(\\.[^.]+)$", ".wav"));
        //音频预处理
        new FFmpegService().audioPreprocessing(audio.toPath(), newAudio.toPath());
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

        CsvUtils.saveCsv(new File("C:\\Users\\11490\\OneDrive\\java\\voicegender\\output-one.csv").toPath(), table);
    }


}
