package io.zrz.jgdb;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class XmlFieldType implements FieldType {

  private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder builder;

  static {
    try {
      builder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean nullable;
  // private int maxlen;
  private String defaultValue;

  @Override
  public Object read(GeoBuffer file) throws IOException {

    int len = file.readVarUInt32();

    byte data[] = new byte[len];

    file.readFully(data);

    Document doc;
    try {
      doc = builder.parse(new ByteArrayInputStream(data));
    } catch (SAXException e) {
      throw new GeoDBException(e);
    }

    return doc;
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }

}