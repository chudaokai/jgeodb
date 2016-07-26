package io.zrz.jgdb;

import java.io.IOException;

import lombok.Builder;
import lombok.Value;

@Value
@Builder class FloatFieldType implements FieldType {

  private boolean nullable;
  private int width;
  private long defaultValue;

  @Override
  public Object read(GeoBuffer file) throws IOException {
    return file.readF32();
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }

}