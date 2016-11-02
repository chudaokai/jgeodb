package io.zrz.jgdb;

import java.io.IOException;

import lombok.ToString;

@ToString
class ObjectIdFieldType implements FieldType {

  public static final ObjectIdFieldType INSTANCE = new ObjectIdFieldType();

  /**
   * Can't read the object ID field ...
   */

  @Override
  public Object read(GeoBuffer file) throws IOException {
    throw new RuntimeException("objid");
  }

  @Override
  public boolean isNullable() {
    return false;
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

  @Override
  public <R> R apply(Object object, GeoValueVisitor<R> converter) {
    throw new RuntimeException();
  }

}