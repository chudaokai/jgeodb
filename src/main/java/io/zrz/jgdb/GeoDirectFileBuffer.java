package io.zrz.jgdb;

import java.io.IOException;
import java.io.RandomAccessFile;

public class GeoDirectFileBuffer implements GeoBuffer {

  private RandomAccessFile raw;
  private long len;
  private int pos;

  public GeoDirectFileBuffer(RandomAccessFile file, long len) {
    this.raw = file;
    this.len = len;
  }

  @Override
  public char readChar() {
    try {
      pos += 2;
      return raw.readChar();
    } catch (IOException ex) {
      throw new GeoDBException(ex);
    }
  }

  @Override
  public float readF32() {
    try {
      pos += 4;
      return raw.readFloat();
    } catch (IOException ex) {
      throw new GeoDBException(ex);
    }
  }

  public short readInt16() {
    try {
      pos += 2;
      return Short.reverseBytes(raw.readShort());
    } catch (IOException ex) {
      throw new GeoDBException(ex);
    }
  }

  @Override
  public int readInt32() {
    return Integer.reverseBytes(readInt());
  }

  @Override
  public double readD64() {
    return Double.longBitsToDouble(Long.reverseBytes(readLong()));
  }

  public int readUnsignedByte() {
    try {
      pos += 1;
      return raw.readUnsignedByte();
    } catch (IOException ex) {
      throw new GeoDBException(ex);
    }
  }

  public byte readByte() {
    try {
      pos += 1;
      return raw.readByte();
    } catch (IOException ex) {
      throw new GeoDBException(ex);
    }
  }

  public int readInt() {
    try {
      pos += 4;
      return raw.readInt();
    } catch (IOException ex) {
      throw new GeoDBException(ex);
    }
  }

  public long readLong() {
    try {
      pos += 8;
      return raw.readLong();
    } catch (IOException ex) {
      throw new GeoDBException(ex);
    }
  }

  @Override
  public int readVarInt32() {

    int ret = 0;
    int shift = 0;

    for (int i = 0; i < 4; ++i) {
      int b = readUnsignedByte();
      ret = ret | ((b & 0x7F) << shift);
      if ((b & 0x80) == 0)
        return ret;
      shift += 7;
    }

    return ret;

  }

  @Override
  public int readVarUInt32() {

    int ret = 0;
    int shift = 0;

    for (int i = 0; i < 4; ++i) {
      int b = readUnsignedByte();
      ret = ret | ((b & 0x7F) << shift);
      if ((b & 0x80) == 0)
        return ret;
      shift += 7;
    }

    return ret;

  }

  @Override
  public long readVarUInt64() {
    int shift = 0;
    long result = 0;
    while (shift < 64) {
      byte b = readByte();
      result |= (long) (b & 0x7F) << shift;
      if ((b & 0x80) == 0)
        return result;
      shift += 7;
    }
    throw new IllegalArgumentException("malformed");
  }

  @Override
  public long readVarInt64() {

    long b = readUnsignedByte();

    long ret = (b & 0x3F);
    int sign = 1;

    if ((b & 0x40) != 0) {
      sign = -1;
    }

    if ((b & 0x80) == 0) {
      return sign * ret;
    }

    int shift = 6;

    while (true) {
      b = readUnsignedByte();
      ret |= (long) ((b & 0x7F) << shift);
      if ((b & 0x80) == 0)
        break;
      shift += 7;
    }

    return sign * ret;

  }

  @Override
  public long remainingBytes() {
    return len - pos;
  }

  @Override
  public void seek(long pos) {
    try {
      this.pos = (int) pos;
      this.raw.seek(pos);
    } catch (IOException ex) {
      throw new GeoDBException(ex);
    }
  }

  @Override
  public void skipBytes(int i) {
    try {
      pos += i;
      raw.skipBytes(i);
    } catch (IOException ex) {
      throw new GeoDBException(ex);
    }
  }

  @Override
  public void readFully(byte[] defaultValue) {
    try {
      pos += defaultValue.length;
      raw.readFully(defaultValue);
    } catch (IOException ex) {
      throw new GeoDBException(ex);
    }
  }

  @Override
  public long getFilePointer() {
    try {
      if (raw.getFilePointer() != pos) {
        throw new IllegalAccessError(String.format("%d != %d", raw.getFilePointer(), pos));
      }
      return raw.getFilePointer();
    } catch (IOException ex) {
      throw new GeoDBException(ex);
    }
  }

  public void close() {
    try {
      this.raw.close();
    } catch (IOException e) {
      // nothing to do.
    }
  }

}
