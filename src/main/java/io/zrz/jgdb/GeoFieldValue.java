package io.zrz.jgdb;

import java.time.Instant;
import java.util.UUID;

import org.w3c.dom.Document;

public class GeoFieldValue {

  private Object value;

  public GeoFieldValue(GeoField field, Object value) {
    this.value = value;
  }

  public boolean isNulled() {
    return this.value == null;
  }

  public String stringValue() {
    return this.value.toString();
  }

  public Instant instantValue() {
    return (Instant) this.value;
  }

  public byte[] binaryValue() {
    return (byte[]) this.value;
  }

  public Document xmlValue() {
    return (Document) this.value;

  }

  public byte byteValue() {
    return (byte) this.value;
  }

  public short shortValue() {
    return (short) this.value;
  }

  public int intValue() {
    if (Long.TYPE.equals(this.value.getClass()))
      return (int) ((long) this.value);
    if (Long.class.equals(this.value.getClass()))
      return (int) ((long) this.value);
    return (int) this.value;
  }

  public long longValue() {
    return (long) this.value;
  }

  //

  public float floatValue() {
    return (float) this.value;
  }

  //

  public double doubleValue() {
    return (double) this.value;
  }

  //

  public UUID uuidValue() {
    return (UUID) this.value;
  }

  //

  public String geometryValue() {
    return null;
  }

  //

  public String toString() {
    if (value != null) {
      return this.value.toString();
    }
    return "(null)";
  }

}
