package cn.cutemc.voicegender.dataset;

import cn.cutemc.voicegender.dataset.records.MagicDataContact;
import cn.cutemc.voicegender.utils.CsvUtils;
import cn.cutemc.voicegender.utils.FFmpegUtils;
import com.opencsv.exceptions.CsvException;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class SpeechSegmenter {

    @Test
    public void segment() throws IOException, CsvException {

        Path datasetsPath = Path.of("F:\\voice_dataset\\Guangzhou_Cantonese_Conversational_Speech_Corpus");

        Path savePath = Path.of(datasetsPath.toString(), "Segment");

        if (!savePath.toFile().exists()) savePath.toFile().mkdir();

        Path contentsPath = Path.of(datasetsPath.toString(), "TXT");

        Path audiosPath = Path.of(datasetsPath.toString(), "WAV");

        List<String> audios = new ArrayList<>();

        for (File file : Objects.requireNonNull(audiosPath.toFile().listFiles())) {
            audios.add(file.getName().replace(".wav", ""));
        }

        List<String[]> table = new ArrayList<>();

        table.add(new String[]{"path", "gender"});

        Thread saveThread = new Thread(() -> {
            while (true) {
                try {
                    CsvUtils.saveCsv(new File(datasetsPath.toFile(), "dataset.csv").toPath(), table);
                } catch (IOException | CsvException e) {
                    throw new RuntimeException(e);
                }

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        saveThread.start();

        for (String audio : audios) {
            File audioFile = new File(audiosPath.toFile(), audio + ".wav");
            File contentFile = new File(contentsPath.toFile(), audio + ".txt");

            Scanner scanner = new Scanner(contentFile);

            List<MagicDataContact> contacts = new ArrayList<>();

            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                List<String> parts = Arrays.stream(line.split("\t")).toList();

                if (parts.get(2).equals("none")) continue;

                String startTime = parts.get(0).replace("[", "").replace("]", "").split(",")[0];
                String endTime = parts.get(0).replace("[", "").replace("]", "").split(",")[1];

                File outputFile = new File(savePath.toFile(), audio + "-[" + startTime + "-" + endTime + "]" + ".wav");

                MagicDataContact contact = new MagicDataContact(outputFile, startTime, endTime, parts.get(1), parts.get(2), parts.get(3));

                contacts.add(contact);

                table.add(new String[]{outputFile.getName(), contact.gender()});
            }

            //循环contacts，每次只处理100个contact
            int size = contacts.size();
            int count = size / 100;
            int mod = size % 100;
            List<List<MagicDataContact>> contactList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                contactList.add(contacts.subList(i * 100, (i + 1) * 100));
            }
            if (mod != 0) {
                contactList.add(contacts.subList(count * 100, count * 100 + mod));
            }

            int i = 0;

            for (List<MagicDataContact> contact : contactList) {

                i++;

                try {
                    FFmpeg ffmpeg = new FFmpeg(FFmpegUtils.getFFmpegPath().get(0));

                    FFmpegBuilder builder = new FFmpegBuilder()
                            .addInput(audioFile.getAbsolutePath());

                    for (int i1 = 1; i1 <= contact.size(); i1++) {
                        builder.addOutput(contact.get(i1 - 1).output().getAbsolutePath())
                                .setFormat("wav")
                                .setAudioSampleRate(FFmpeg.AUDIO_SAMPLE_16000)
                                .setAudioFilter("atrim=" + contact.get(i1 - 1).startTime() + ":" + contact.get(i1 - 1).endTime())
                                .done();
                    }

                    FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);

                    FFmpegJob job = executor.createJob(builder);

                    job.run();

                    while (job.getState() != FFmpegJob.State.FINISHED) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //输出剩余的文件和contact
                System.out.println("剩余文件：" + audios.size() + " 剩余对话: " + (contactList.size() - i) * 100 + "/" + contactList.size() * 100);
            }


        }

    }

    @Test
    public void single() throws IOException {

        List<Thread> threads = new ArrayList<>();

        List<MagicDataContact> contacts = new ArrayList<>();

        File output = new File("F:\\voice_dataset\\Guangzhou_Cantonese_Conversational_Speech_Corpus\\Segment", "Test.wav");

        contacts.add(new MagicDataContact(output, "0.00", "0.100", "M", "F", "F"));

        FFmpeg ffmpeg = new FFmpeg(FFmpegUtils.getFFmpegPath().get(0));

        FFmpegBuilder builder = new FFmpegBuilder()
                .addInput("");

        for (int i = 1; i <= contacts.size(); i++) {
            builder.addOutput(contacts.get(i - 1).output().getAbsolutePath())
                    .setFormat("wav")
                    .setAudioSampleRate(FFmpeg.AUDIO_SAMPLE_16000)
                    .setAudioFilter("atrim=" + contacts.get(i - 1).startTime() + ":" + contacts.get(i - 1).endTime())
                    .done();
        }

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);

        FFmpegJob job = executor.createJob(builder);

        job.run();

        while (job.getState() != FFmpegJob.State.FINISHED) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

