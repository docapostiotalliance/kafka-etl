package org.kafka.etl.kafka.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class TestUtils {
    public static String getStringFromResourceFile(String filePath) throws IOException {

        if (filePath != null && !filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }

        InputStream is = StringUtils.class.getResourceAsStream(filePath);
        if (is == null) {
            throw new FileNotFoundException();
        }

        return IOUtils.toString(is);
    }
}
