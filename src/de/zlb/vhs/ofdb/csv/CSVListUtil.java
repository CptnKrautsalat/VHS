package de.zlb.vhs.ofdb.csv;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import de.zlb.vhs.ofdb.FilmEntry;
import de.zlb.vhs.ofdb.FilmVersionEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CSVListUtil {
    public static final Logger log = LogManager.getLogger(CSVListUtil.class);
    public static final char SEPARATOR = ',';

    public static List<FilmVersionEntryBean> readOFDBListFromCSV(File csvFile) throws IOException {

        log.info("Reading OFDB list from file...");

        FileReader reader = new FileReader(csvFile);

        CsvToBeanBuilder<FilmVersionEntryBean> builder = new CsvToBeanBuilder<>(reader);
        CsvToBean<FilmVersionEntryBean> beanReader = builder
                .withSeparator(SEPARATOR)
                .withType(FilmVersionEntryBean.class)
                .build();

        List<FilmVersionEntryBean> beans = beanReader
                .stream()
                .collect(Collectors.toList());

        log.info("Read " + beans.size() + " beans from file!");

        reader.close();

        return beans;
    }

    public static void writeOFBDListToCSV(Collection<FilmEntry> films, String file) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        log.info("Writing OFDB list to CSV file...");

        FileWriter writer = new FileWriter(file);

        StatefulBeanToCsvBuilder<FilmVersionEntryBean> builder = new StatefulBeanToCsvBuilder<>(writer);
        StatefulBeanToCsv<FilmVersionEntryBean> beanWriter = builder
            .withSeparator(SEPARATOR)
            .build();

        List<FilmVersionEntryBean> beans = films
                .stream()
                .flatMap(FilmEntry::getVersions)
                .map(FilmVersionEntry::toBean)
                .collect(Collectors.toList());

        log.info("Writing " + beans.size() + " releases of " + films.size() + " films.");

        beanWriter.write(beans);
        writer.close();

        log.info("Done writing!");
    }
}
