module de.zlb.vhs {
    exports de.zlb.vhs;
    exports de.zlb.vhs.catalog;
    exports de.zlb.vhs.ofdb;
    exports de.zlb.vhs.csv;
    exports de.zlb.vhs.ofdb.web;

    requires org.jsoup;
    requires org.apache.logging.log4j;
    requires opencsv;
    requires java.sql;
    requires com.google.common;
    requires static lombok;
}