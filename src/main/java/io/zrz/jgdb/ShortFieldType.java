package io.zrz.jgdb;

import java.io.IOException;

import lombok.Builder;
import lombok.Value;

@Value
@Builder class ShortFieldType implements FieldType {

  private boolean nullable;
  private int width;
  private Short defaultValue;

  @Override
  public Object read(GeoBuffer file) throws IOException {
    return file.readInt16();
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }
  
  @Override
  public <R> R apply(Object object, GeoValueVisitor<R> converter) {
    return converter.visitShort((short)object);
  }


}