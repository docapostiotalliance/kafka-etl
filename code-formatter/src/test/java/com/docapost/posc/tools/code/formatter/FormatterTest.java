package com.docapost.posc.tools.code.formatter;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.text.BadLocationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class FormatterTest {

  private Formatter formatter;

  @Before
  public void init() throws IOException, SAXException, ParserConfigurationException {
    formatter = new Formatter(FormatterApp.CODE_STYLE_FILENAME);
  }

  @Test
  public void testFormatCode() throws BadLocationException {
    String expected =
        "package com.docapost.posc.tools.code.formatter;\n\nimport java.io.IOException;\n\npublic class FormatterTest {\n}\n";
    String fileContent =
        "package com.docapost.posc.tools.code.formatter; import java.io.IOException; public class FormatterTest {}";

    String formattedCode = formatter.format(fileContent);

    Assert.assertEquals(expected, formattedCode);
  }

}
