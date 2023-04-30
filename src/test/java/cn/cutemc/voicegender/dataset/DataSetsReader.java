package cn.cutemc.voicegender.dataset;

import cn.cutemc.voicegender.utils.CsvUtils;
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class DataSetsReader {

    @Test
    public void read() throws IOException, CsvException {

        Path dataSet = Path.of("F:\\voice_dataset\\ST-CMDS-20170001_1-OS");

        if (!dataSet.toFile().exists()) {
            throw new RuntimeException("Datasets not exists");
        }

        List<File> metadataList = new LinkedList<>(Arrays.asList(Objects.requireNonNull(dataSet.toFile().listFiles((dir, name) -> name.endsWith(".metadata")))));

        List<String[]> table = new LinkedList<>();

        table.add(new String[]{"Path", "Gender"});

        int i = 0;

        for (File metadataFile : metadataList) {
            i++;
            Scanner scanner = new Scanner(metadataFile);

            String gender = null;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("SEX")) {
                    if (line.endsWith("男")) {
                        gender = "male";
                    } else if (line.endsWith("女")) {
                        gender = "female";
                    }
                }
            }

            if (gender != null) {
                table.add(new String[] {metadataFile.getName().replace(".metadata", ".wav"), gender});
            }

            System.out.println("剩余文件数: " + (metadataList.size() - i) + " / " + metadataList.size());
        }

        System.out.println("生成CSV中...");

        CsvUtils.saveCsv(new File("F:\\voice_dataset", "dataset.csv").toPath(), table);

    }


}
