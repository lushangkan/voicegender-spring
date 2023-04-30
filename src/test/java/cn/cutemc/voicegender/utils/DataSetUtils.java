package cn.cutemc.voicegender.utils;

import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class DataSetUtils {

    /**
     * 读取数据集Csv
     * @param dataSetPath 数据集路径
     * @param csvName Csv文件名
     * @return 数据集
     * @throws IOException IO异常
     * @throws CsvException Csv异常
     */
    public static Map<String, String> getDataSet(Path dataSetPath, String csvName) throws IOException, CsvException {
        List<String[]> allLines = CsvUtils.readCsv(new File(dataSetPath.toFile(), csvName).toPath());

        Map<String, String> dataset = allLines.stream()
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));

        dataset.values().removeIf(String::isEmpty);

        dataset.values().removeIf(s -> !Objects.equals(s, "male") && !Objects.equals(s, "female"));

        return dataset;
    }


    /**
     * Map随机抽样,50%女,50%男
     * @param map 原始Map
     * @param sample 抽样数量
     * @return 抽样结果
     * @param <K> key类型
     * @param <V> value类型
     */
    public static <K, V> Map<K, V> sample(Map<K, V> map, int sample) {
        if (map == null || map.size() == 0) {
            return null;
        }
        if (sample <= 0) {
            return null;
        }
        if (sample > map.size()) {
            return null;
        }

        //提取男性
        Map<K, V> male = new HashMap<>(map);
        male.values().removeIf(v -> v.equals("female"));

        //提取女性
        Map<K, V> female = new HashMap<>(map);
        female.values().removeIf(v -> v.equals("male"));

        //计算男性数量
        int maleSample = sample / 2;
        int femaleSample = sample - maleSample;

        Map<K, V> result = new HashMap<>();

        int i = 0;
        while (i < 2) {
            if (i == 0) {
                //男性
                List<K> keys = new ArrayList<>(male.keySet());
                Random random = new Random();
                for (int i1 = 0; i1 < maleSample; i1++) {
                    if (keys.size() <= 0)  break;
                    int index = random.nextInt(keys.size());
                    result.put(keys.get(index), map.get(keys.get(index)));
                    keys.remove(index);
                }
            } else if (i == 1) {
                //女性
                List<K> keys = new ArrayList<>(female.keySet());
                Random random = new Random();
                for (int i1 = 0; i1 < femaleSample; i1++) {
                    if (keys.size() <= 0)  break;
                    int index = random.nextInt(keys.size());
                    result.put(keys.get(index), map.get(keys.get(index)));
                    keys.remove(index);
                }
            }
            i++;
        }

        return result;
    }
}
