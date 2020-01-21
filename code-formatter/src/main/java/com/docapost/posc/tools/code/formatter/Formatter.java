package com.docapost.posc.tools.code.formatter;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.xml.sax.SAXException;

public class Formatter {

  private final CodeFormatter formatter;

  public Formatter(final String xmlFile)
      throws IOException, SAXException, ParserConfigurationException {
    CodeStyleParser parser = new CodeStyleParser();
    formatter = new DefaultCodeFormatter(parser.toProperties(xmlFile));
  }

  public String format(final String input) throws BadLocationException {
    final IDocument doc = new Document();
    doc.set(input);
    final TextEdit edit =
        formatter.format(CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS,
            input,
            0,
            input.length(),
            0,
            "\n");
    if (edit == null) {
      throw new RuntimeException("formatting failed");
    }
    edit.apply(doc);
    return doc.get();
  }
}
