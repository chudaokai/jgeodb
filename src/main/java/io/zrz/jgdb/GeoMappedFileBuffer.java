package io.zrz.jgdb;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

public class GeoMappedFileBuffer implements GeoBuffer {

  private long len;
  private int pos;
  private MappedByteBuffer map;
  private RandomAccessFile file;

  public GeoMappedFileBuffer(RandomAccessFile file, long len) {
    this.file = file;
    try {
      this.map = file.getChannel().map(MapMode.READ_ONLY, 0, file.length());
    } catch (IOException e) {
      throw new GeoDBException(e);
    }
    this.len = len;
  }

  @Override
  public char readChar() {
    char ch = map.getChar(pos);
    pos += 2;
    return ch;
  }

  @Override
  public float readF32() {
    float ch = map.getFloat(pos);
    pos += 4;
    return ch;
  }

  private short readShort() {
    short ch = map.getShort(pos);
    pos += 2;
    return ch;
  }

  public int readUnsignedByte() {
    return readByte() & 0xFF;
  }

  public byte readByte() {
    byte ch = map.get(pos);
    pos += 1;
    return ch;
  }

  public int readInt() {
    int ch = map.getInt(pos);
    pos += 4;
    return ch;
  }

  public long readLong() {
    long ch = map.getLong(pos);
    pos += 8;
    return ch;
  }

  //

  public short readInt16() {
    return Short.reverseBytes(readShort());
  }

  @Override
  public int readInt32() {
    return Integer.reverseBytes(readInt());
  }

  @Override
  public long readUInt32() {
    return Integer.reverseBytes(readInt()) & 0x00000000ffffffffL;
  }

  @Override
  public double readD64() {
    return Double.longBitsToDouble(Long.reverseBytes(readLong()));
  }

  @Override
  public int readVarUInt32() {
    int shift = 0;
    int result = 0;
    while (shift < 32) {
      byte b = readByte();
      result |= (int) (b & 0x7F) << shift;
      if ((b & 0x80) == 0)
        return result;
      shift += 7;
    }
    throw new IllegalArgumentException("malformed");
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
  public int readVarInt32() {

    int b = readUnsignedByte();

    int ret = (b & 0x3F);
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
      ret |= (int) ((b & 0x7F) << shift);
      if ((b & 0x80) == 0)
        break;
      shift += 7;
    }

    return sign * ret;

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
    this.pos = (int) pos;
  }

  @Override
  public void skipBytes(int i) {
    pos += i;
  }

  @Override
  public void readFully(byte[] defaultValue) {
    map.position(this.pos);
    map.get(defaultValue, 0, defaultValue.length);
    map.position(map.position() - defaultValue.length);
    pos += defaultValue.length;
  }

  @Override
  public long getFilePointer() {
    return this.pos;
  }

  public void close() {
    try {
      this.file.close();
    } catch (IOException e) {
      // nothing to do.
    }
  }

  @Override
  public int readUInt8() {
    return readUnsignedByte();
  }

}
