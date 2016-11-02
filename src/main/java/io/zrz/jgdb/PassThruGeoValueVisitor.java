package io.zrz.jgdb;

import java.time.Instant;
import java.util.UUID;

import org.w3c.dom.Document;

import io.zrz.jgdb.shape.GeometryValue;

public abstract class PassThruGeoValueVisitor<R> implements GeoValueVisitor<Object> {

  @Override
  public Object visitString(String value) {
    return value;
  }

  @Override
  public Object visitLong(long value) {
    return value;
  }

  @Override
  public Object visitInteger(int value) {
    return value;
  }

  @Override
  public Object visitShort(short value) {
    return value;
  }

  @Override
  public Object visitDouble(double value) {
    return value;
  }

  @Override
  public Object visitFloat(float value) {
    return value;
  }

  @Override
  public Object visitInstant(Instant value) {
    return value;
  }

  @Override
  public Object visitGeometry(GeometryValue value) {
    return value;
  }

  @Override
  public Object visitBinary(byte[] value) {
    return value;
  }

  @Override
  public Object visitUUID(UUID value) {
    return value;
  }

  @Override
  public Object visitXml(Document value) {
    return value;
  }

}
