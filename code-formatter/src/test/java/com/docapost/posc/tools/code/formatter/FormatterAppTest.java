package com.docapost.posc.tools.code.formatter;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.text.BadLocationException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class FormatterAppTest {

  @Before
  public void init() throws IOException, SAXException, ParserConfigurationException {}

  @Test(expected = NoSuchFileException.class)
  public void testFormatCodeWithInvalidFile() throws BadLocationException, IOException {
    String[] args = {"FormatterTest.java"};
    FormatterApp.main(args);
  }
}
