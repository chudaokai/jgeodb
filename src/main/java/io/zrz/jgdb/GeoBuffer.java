package io.zrz.jgdb;

public interface GeoBuffer {

  void seek(long pos);

  int readInt32();

  int readVarUInt32();

  long readVarUInt64();

  long readVarInt64();

  long remainingBytes();

  void skipBytes(int i);

  double readD64();

  void readFully(byte[] defaultValue);

  long getFilePointer();

  char readChar();

  int readVarInt32();

  short readInt16();

  float readF32();

}
