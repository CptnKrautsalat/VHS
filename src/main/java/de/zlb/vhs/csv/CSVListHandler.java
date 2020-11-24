package de.zlb.vhs.csv;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVListHandler<B> {
    public static final Logger log = LogManager.getLogger(CSVListHandler.class);
    private final char separator;

    public CSVListHandler(char separator) {
        this.separator = separator;
    }

    public List<B> readListFromCSVFile(File csvFile, Class<? extends B> type) throws IOException {

        log.info("Reading list from file " + csvFile.getName() + " ...");

        FileReader reader = new FileReader(csvFile);

        CsvToBeanBuilder<B> builder = new CsvToBeanBuilder<>(reader);
        CsvToBean<B> beanReader = builder
                .withSeparator(separator)
                .withType(type)
                .build();

        List<B> beans = beanReader
                .stream()
                .collect(Collectors.toList());

        log.info("Read " + beans.size() + " beans from " + csvFile.getName() + "!");

        reader.close();

        return beans;
    }

    public void writeListToCSVFile(List<B> beans, String file) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        log.trace("Writing list to CSV file " + file + "...");

        FileWriter writer = new FileWriter(file);

        StatefulBeanToCsvBuilder<B> builder = new StatefulBeanToCsvBuilder<>(writer);
        StatefulBeanToCsv<B> beanWriter = builder
            .withSeparator(separator)
            .build();

        log.trace("Writing " + beans.size() + " releases.");

        beanWriter.write(beans);
        writer.close();

        log.trace("Done writing " + file + "!");
    }

    public void readListFromFile(String fileName, Consumer<List<B>> listConsumer, Class<? extends B> type) {
        File csvFile = new File(fileName);
        try {
            listConsumer.accept(readListFromCSVFile(csvFile, type));
        } catch (IOException e) {
            log.error("Failed to read file " + fileName, e);
        }
    }

    public void readListFromDirectory(String directory, Consumer<List<B>> listConsumer, Class<? extends B> type) {
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            paths
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .forEach(fileName -> readListFromFile(fileName, listConsumer, type));
        } catch (IOException e) {
            log.error("Failed to read files in directory " + directory, e);
        }
    }
}
