package io.zrz.jgdb;

import java.time.Instant;
import java.util.UUID;

import org.w3c.dom.Document;

import io.zrz.jgdb.shape.GeometryValue;

public interface GeoValueVisitor<R> {

  R visitString(String value);

  R visitLong(long value);

  R visitInteger(int value);

  R visitShort(short value);

  R visitDouble(double value);

  R visitFloat(float value);

  R visitInstant(Instant value);

  R visitGeometry(GeometryValue value);

  R visitBinary(byte[] value);

  R visitUUID(UUID value);

  R visitXml(Document value);

}
