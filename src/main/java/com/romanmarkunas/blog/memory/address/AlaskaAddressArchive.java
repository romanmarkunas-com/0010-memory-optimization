package com.romanmarkunas.blog.memory.address;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Arrays.asList;

public final class AlaskaAddressArchive {

    private static final String ZIP_LOCATION = "src/main/resources/alaska-addresses.zip";
    private static final String DESTINATION_DIR = "src/main/resources/alaska-addresses/";
    private static final List<String> ADDRESS_FILES = asList(
            "ak/anchorage.csv",
            "ak/fairbanks_north_star_borough.csv",
            "ak/haines.csv",
            "ak/kenai_peninsula_borough.csv",
            "ak/kodiak_island_borough.csv"
    );


    private AlaskaAddressArchive() {}


    public static List<Address> read() {
        unzip();
        return readAddresses();
    }


    private static void unzip() {
        File destDir = new File(DESTINATION_DIR);
        if (destDir.exists()) {
            return;
        }

        System.out.println("Unzipping archive");

        try (ZipInputStream in = new ZipInputStream(new FileInputStream(ZIP_LOCATION))) {
            ZipEntry zipEntry;
            while ((zipEntry = in.getNextEntry()) != null) {
                writeEntry(destDir, in, zipEntry);
            }
        }
        catch (IOException e) {
            System.out.println(
                    "Exception during unzipping, please delete destination "
                  + "directory manually if it exists, before re-running"
            );
            throw new RuntimeException(e);
        }

        System.out.println("Done unzipping");
    }

    private static void writeEntry(File destDir, ZipInputStream in, ZipEntry entry) throws IOException {
        File entryFile = new File(destDir, entry.getName());
        if (entry.isDirectory()) {
            if (!entryFile.mkdirs()) {
                throw new IOException("Unable to create folder " + entry);
            }
        }
        else {
            try (FileOutputStream out = new FileOutputStream(entryFile)) {
                byte[] buffer = new byte[10 * 1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
        }
    }

    private static List<Address> readAddresses() {
        ArrayList<Address> out = new ArrayList<>();
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        ObjectReader reader = csvMapper.readerFor(Address.class).with(schema);

        for (String file : ADDRESS_FILES) {
            File csvFile = new File(DESTINATION_DIR, file);

            MappingIterator<Address> iterator;
            try {
                iterator = reader.readValues(csvFile);
            }
            catch (IOException e) {
                throw new RuntimeException("Unable to parse addresses!", e);
            }

            while (iterator.hasNext()) {
                out.add(iterator.next());
            }
        }

        return out;
    }
}
