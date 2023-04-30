package cn.cutemc.voicegender.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class CsvUtils {

    public static LinkedList<String[]> readCsv(Path path) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new FileReader(path.toFile()));

        return (LinkedList<String[]>) reader.readAll();
    }

    public static void saveCsv(Path path, List<String[]> table) throws IOException, CsvException {
        CSVWriter writer = new CSVWriter(new FileWriter(path.toFile()));

        writer.writeAll(table);
        writer.close();
    }
}
