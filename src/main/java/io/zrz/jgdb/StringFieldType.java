package io.zrz.jgdb;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class StringFieldType implements FieldType {

  private boolean nullable;
  private int maxlen;
  private String defaultValue;

  @Override
  public Class<?> getJavaType() {
    return String.class;
  }
  @Override
  public Object read(GeoBuffer file) throws IOException {

    int clen = file.readVarUInt32();

    if (clen == 0) {
      return null;
    }

    byte data[] = new byte[clen];
    file.readFully(data);

    String str = new String(data, StandardCharsets.UTF_8);
    return str;

  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }
  
  @Override
  public <R> R apply(Object object, GeoValueVisitor<R> converter) {
    return converter.visitString((String)object);
  }


}