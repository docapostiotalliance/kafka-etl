package com.docapost.posc.tools.code.formatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.eclipse.jface.text.BadLocationException;


public class FormatterApp {

  public static final String CODE_STYLE_FILENAME = "code-style/code-style.xml";
  public static final String CODE_STYLE_KEY = "codeStyle";
  private static Formatter formatter;

  public static void main(final String[] args) throws IOException, BadLocationException {
    String codeStyle = CODE_STYLE_FILENAME;
    if (System.getProperties().containsKey(CODE_STYLE_KEY)) {
      codeStyle = System.getProperty(CODE_STYLE_KEY);
    }

    if (args.length < 1) {
      System.err.println("Missing arguments : [file1.java file2.java ...]");
      System.exit(1);
    }

    Set<String> mySet = new TreeSet<String>(Arrays.asList(args));

    int fileCount = 1;
    int fileTotal = mySet.size();
    try {
      System.out.println("Start formatting code using " + codeStyle
          + ", there is "
          + fileTotal
          + " files to process");
      formatter = new Formatter(codeStyle);
    } catch (Exception e) {
      System.err.println(e.toString());
      System.exit(2);
    }

    int formattedFileNumber = 0;

    for (final String file : mySet) {
      System.out.println("Formatting [" + fileCount++ + "/" + fileTotal + "] : " + file);
      if (format(file)) {
        formattedFileNumber++;
      }
    }

    if (formattedFileNumber > 0) {
      System.out.println("CAUTION: " + formattedFileNumber + " fomatted files...");
    }
  }

  private static boolean format(String file) throws IOException, BadLocationException {
    Path path = Paths.get(file);
    String fileContent = new String(Files.readAllBytes(path));
    String formattedContent = formatter.format(fileContent);
    Files.write(path, formattedContent.getBytes());
    String fileContent2 = new String(Files.readAllBytes(path));
    return !fileContent.equals(fileContent2);
  }

}
