package org.kafka.etl.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileHelper.class);

  public static void validateEntryPath(File file, String regularPath) {
    String absolutePath = file.getAbsolutePath();
    LOGGER.info("Validating path {} ...", absolutePath);
    if (!absolutePath.equals(regularPath)) {
      LOGGER.error("This path {} passed is not authorized to access!!!", absolutePath);
      throw new SecurityException(
          "This path " + absolutePath + " passed is not authorized to access!!!");
    }
  }

  /**
   * Checking if a file exists.
   *
   * @param path
   * @return true if the file exists, otherwise false
   */
  public static boolean existFile(String path) {
    File f = new File(path);
    validateEntryPath(f, path);
    return f.exists();
  }

  /**
   * Getting the file content into a String.
   *
   * @param pathFile
   * @return the content
   */
  public static String file2stringQuietly(String pathFile) {
    if (!existFile(pathFile)) {
      LOGGER.warn("[file2string] Le fichier {} n'existe pas !", pathFile);
      return null;
    }

    try {
      File file = new File(pathFile);
      validateEntryPath(file, pathFile);
      FileInputStream fis = new FileInputStream(file);
      return file2stringQuietly(fis);
    } catch (IOException e) {
      LOGGER.error("Erreur à la lecture du fichier ", e);
      return null;
    }
  }

  private static String file2stringQuietly(InputStream fis) {
    BufferedReader reader;
    try {
      reader = new BufferedReader(new InputStreamReader(fis, "UTF8"));

      StringBuilder builder = new StringBuilder();
      String line;

      // For every line in the file, append it to the string builder
      while (null != (line = reader.readLine())) {
        builder.append(line);
      }

      reader.close();
      return builder.toString();
    } catch (IOException e) {
      LOGGER.error("Erreur à la lecture du fichier ", e);
      return null;
    }
  }

  /**
   * Private constructor => utils class.
   */
  private FileHelper() {
    // Nothing to do.
  }
}

