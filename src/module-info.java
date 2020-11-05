module VHSCollection {
    exports de.zlb.vhs.ofdb;
    exports de.zlb.vhs.ofdb.csv;
    exports de.zlb.vhs.ofdb.web;

    requires org.jsoup;
    requires org.apache.logging.log4j;
    requires opencsv;
    requires java.sql;
}