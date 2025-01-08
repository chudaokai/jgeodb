package io.zrz.jgdb;

import java.io.IOException;

import lombok.Builder;
import lombok.Value;

@Value
@Builder class DoubleFieldType implements FieldType {

  private boolean nullable;
  private int width;
  private double defaultValue;
  @Override
  public Class<?> getJavaType() {
    return double.class;
  }

  @Override
  public Object read(GeoBuffer file) throws IOException {
    return file.readD64();
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }
  
  @Override
  public <R> R apply(Object object, GeoValueVisitor<R> converter) {
    return converter.visitDouble((double)object);
  }


}