package cn.cutemc.voicegender.analyze;

import lombok.extern.apachecommons.CommonsLog;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Service
@CommonsLog
public class FFmpegService {
    private final FFmpeg ffmpeg;

    private final FFmpegExecutor executor;

    private final FFprobe ffprobe;

    public FFmpegService() {
        try {
            ffmpeg = new FFmpeg();
            executor = new FFmpegExecutor(ffmpeg);
        } catch (Exception e) {
            log.fatal("Unable to create FFmpeg instance");
            throw new Error(e);
        }

        try {
            ffprobe = new FFprobe();
        } catch (IOException e) {
            log.fatal("Unable to create FFprobe instance");
            throw new Error(e);
        }

    }

    /**
     * 音频预处理
     * 1. 使用带通滤波器，去除0hz-280hz以外的音频
     * 2. 转换为WAV格式
     * @param input 输入文件
     * @param output 输出文件，必须是文件，不能是文件夹
     * @return 输出文件
     * @throws IOException IO异常
     */
    public Path audioPreprocessing(Path input, Path output) throws IOException {

        output = new File(output.toString().replaceAll("\\.\\w+$", ".wav")).toPath();

        FFmpegBuilder builder = new FFmpegBuilder()
                .addInput(input.toString())
                .addOutput(output.toString())
                .setFormat("wav")
                .setAudioSampleRate(FFmpeg.AUDIO_SAMPLE_16000)
                .setAudioFilter("bandpass=f=140:width_type=h:w=140:n")
                .done();

        FFmpegJob job = executor.createJob(builder);

        job.run();

        return output;
    }

    /**
     * 获取音频时长(秒)
     * @param path 音频文件
     * @return 音频时长(秒)
     * @throws IOException IO异常
     */
    public double getDuration(Path path) throws IOException {
        return ffprobe.probe(path.toAbsolutePath().toString()).getFormat().duration;
    }

    /**
     * 音频分割
     * @param input 输入文件
     * @param output 输出文件
     * @param startTime 开始时间(秒)
     * @param endTime 结束时间(秒)
     * @return 输出文件
     * @throws IOException IO异常
     */
    public Path audioSegmentation(Path input, Path output, String startTime, String endTime) throws IOException {
        FFmpegBuilder builder = new FFmpegBuilder()
                .addInput(input.toAbsolutePath().toString())
                .addOutput(output.toAbsolutePath().toString())
                .setFormat("wav")
                .setAudioSampleRate(FFmpeg.AUDIO_SAMPLE_16000)
                .setAudioFilter("atrim=" + startTime + ":" + endTime)
                .done();

        FFmpegJob job = executor.createJob(builder);

        job.run();

        return output;
    }


}
