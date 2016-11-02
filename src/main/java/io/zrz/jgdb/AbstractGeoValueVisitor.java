package io.zrz.jgdb;

import java.time.Instant;
import java.util.UUID;

import org.w3c.dom.Document;

import io.zrz.jgdb.shape.GeometryValue;

public abstract class AbstractGeoValueVisitor<R> implements GeoValueVisitor<R> {

  @Override
  public R visitString(String value) {
    return null;
  }

  @Override
  public R visitLong(long value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public R visitInteger(int value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public R visitShort(short value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public R visitDouble(double value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public R visitFloat(float value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public R visitInstant(Instant value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public R visitGeometry(GeometryValue value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public R visitBinary(byte[] object) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public R visitUUID(UUID object) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public R visitXml(Document object) {
    // TODO Auto-generated method stub
    return null;
  }

}
