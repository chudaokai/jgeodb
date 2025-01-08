package io.zrz.jgdb;

import java.io.IOException;
import java.util.UUID;

import lombok.ToString;

@ToString
public class UUIDFieldType implements FieldType {

  public static final UUIDFieldType NULLABLE_INSTANCE = new UUIDFieldType(true);
  public static final UUIDFieldType INSTANCE = new UUIDFieldType(false);
  private boolean nullable;

  @Override
  public Class<?> getJavaType() {
    return String.class;
  }
  public UUIDFieldType(boolean nullable) {
    this.nullable = nullable;
  }

  @Override
  public Object read(GeoBuffer file) throws IOException {
    byte b[] = new byte[16];
    file.readFully(b);
    return UUID.fromString(
        String.format("%02X%02X%02X%02X-%02X%02X-%02X%02X-%02X%02X-%02X%02X%02X%02X%02X%02X", b[3], b[2], b[1], b[0], b[5], b[4], b[7], b[6], b[8], b[9], b[10], b[11],
            b[12], b[13], b[14], b[15]));
  }

  @Override
  public boolean isNullable() {
    return nullable;
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

  @Override
  public <R> R apply(Object object, GeoValueVisitor<R> converter) {
    return converter.visitUUID((UUID)object);
  }

}