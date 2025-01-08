package io.zrz.jgdb;

import java.io.IOException;

import lombok.Builder;
import lombok.Value;

@Value
@Builder class BinaryFieldType implements FieldType {

  private boolean nullable;

  @Override
  public Class<?> getJavaType() {
    return byte[].class;
  }

  @Override
  public Object read(GeoBuffer file) throws IOException {
    int len = file.readVarUInt32();
    byte data[] = new byte[len];
    file.readFully(data);
    return data;
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

  @Override
  public <R> R apply(Object object, GeoValueVisitor<R> converter) {
    return converter.visitBinary((byte[])object);
  }

}