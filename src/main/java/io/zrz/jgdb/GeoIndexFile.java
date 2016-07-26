package io.zrz.jgdb;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.function.Predicate;

import lombok.Builder;
import lombok.Value;

public class GeoIndexFile {

  @Builder
  @Value
  private static class Header {

    // number of blocks of offsets for 1024 features that are effectively
    // present in that file (ie sparse blocks are not counted in that number).
    int n1024BlocksPresent;

    // number of rows, included deleted rows
    int numberOfRows;

    // number of bytes to encode each feature offset. Must be 4 (.gdbtable up to
    // 4GB), 5 (.gdbtable up to 1TB) or 6 (.gdbtable up to 256TB)
    int sizeOffset;

  }

  private final GeoBuffer reader;
  private Header header;
  private RandomAccessFile file;
  private BitSet blockmap;

  public GeoIndexFile(final Path path) throws IOException {
    
    this.file = new RandomAccessFile(path.toFile(), "r");
    this.reader = new GeoMappedFileBuffer(this.file, file.length());
    this.header = this.readHeader();

    if (this.header.getSizeOffset() < 4 || header.getSizeOffset() > 6) {
      throw new GeoDBException(String.format("Size offset %d not implemented yet", this.header.getSizeOffset()));
    }

    if (header.getN1024BlocksPresent() != 0) {

      // seek
      reader.seek(16 + header.getSizeOffset() * header.getN1024BlocksPresent() * 1024);

      int magic = (int) reader.readUInt32();
      int nBitsForBlockMap = (int) reader.readUInt32();

      if (nBitsForBlockMap > (Integer.MAX_VALUE / 1024)) {
        throw new GeoDBException("Corrupt DB");
      }

      //
      int n1024BlocksBis = (int) reader.readUInt32();

      if (n1024BlocksBis != header.getN1024BlocksPresent()) {
        throw new GeoDBException("Corrupt DB");
      }

      @SuppressWarnings("unused")
      int nUsefulBitmapIn32Words = reader.readInt32();

      if (magic == 0) {

        if (nBitsForBlockMap != header.getN1024BlocksPresent()) {
          throw new GeoDBException("Corrupt DB");
        }

      } else {

        if (header.getNumberOfRows() > (nBitsForBlockMap * 1024)) {
          throw new GeoDBException("Corrupt DB");
        }

        int nSizeInBytes = (((nBitsForBlockMap) + 7) / 8);

        // so, read that many bytes.
        byte[] data = new byte[nSizeInBytes];
        reader.readFully(data);
        this.blockmap = BitSet.valueOf(data);

        int nCountBlocks = 0;

        for (int i = 0; i < nBitsForBlockMap; i++) {
          nCountBlocks += blockmap.get(i) ? 1 : 0;
        }

        if (nCountBlocks != header.getN1024BlocksPresent()) {
          throw new GeoDBException(String.format("free page bitmap corrupt? %d != %d", nCountBlocks, header.getN1024BlocksPresent()));
        }

      }

    }

  }

  public Header readHeader() throws IOException {

    final int magic = Integer.reverseBytes(reader.readInt32());

    if (magic != 0x3000000) {
      throw new IllegalArgumentException("Invalid Magic");
    }

    final Header.HeaderBuilder b = Header.builder();

    b.n1024BlocksPresent(reader.readInt32());
    b.numberOfRows(reader.readInt32());
    b.sizeOffset(reader.readInt32());

    return b.build();

  }

  public void scan(Predicate<Long> acceptor, OffsetConsumer listener) {

    int id = 0;
    int missing = 0;

    while (id < this.header.getNumberOfRows()) {

      if (this.blockmap != null) {

        int iBlock = id / 1024;

        if (!blockmap.get(iBlock)) {
          id += 1024;
          missing += 1024;
          continue;
        }

      }

      if (acceptor.test((long) id)) {

        long offset = readIndexAt(id - missing);

        if (offset >= 0) {
          listener.accept(id + 1, offset);
        }

      }

      ++id;

    }

  }

  int getFeatureOffset(int id) {

    if (id < 0 || id >= header.getNumberOfRows()) {
      return -1;
    }

    if (this.blockmap != null) {

      int iBlock = id / 1024;

      if (!blockmap.get(iBlock)) {
        return -1;
      }

      int nCountBlocksBefore = 0;

      for (int i = 0; i < iBlock; i++) {
        nCountBlocksBefore += blockmap.get(i) ? 1 : 0;
      }

      id = (nCountBlocksBefore * 1024) + (id % 1024);

    }

    return readIndexAt(id);

  }

  private int readIndexAt(int id) {
    
    int pos = (16 + id * this.header.getSizeOffset());

    reader.seek(pos);

    long offset = reader.readInt32();

    switch (header.getSizeOffset()) {
      case 4:
        break;
      case 5:
        offset |= ((int) reader.readUInt8()) << 32;
        break;
      case 6:
        offset |= ((int) reader.readUInt8()) << 32;
        offset |= ((int) reader.readUInt8()) << 40;
        break;
      default:
        throw new GeoDBException("internal error");

    }

    if (offset <= 0 || offset >= Integer.MAX_VALUE) {
      return -1;
    }

    return (int) offset;
  }

  public void close() {
    try {
      this.file.close();
    } catch (final IOException e) {
      // nothing ..
    }
  }

}
