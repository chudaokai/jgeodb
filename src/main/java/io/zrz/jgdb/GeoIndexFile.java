package io.zrz.jgdb;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
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

  public GeoIndexFile(final Path path) throws IOException {
    this.file = new RandomAccessFile(path.toFile(), "r");
    this.reader = new GeoMappedFileBuffer(this.file, file.length());
    this.header = this.readHeader();
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

    long id = 0;

    while (id < this.header.getNumberOfRows()) {

      if (acceptor.test(id)) {

        long pos = (16 + id * this.header.getSizeOffset());

        reader.seek(pos);

        long offset = reader.readInt32();

        if (offset < 0) {
          throw new IllegalArgumentException();
        }

        if (offset > 0) {
          listener.accept(id + 1, offset);
        }

      }

      ++id;

    }

  }

  int getFeatureOffset(int id) throws IOException {
    int pos = (16 + id * this.header.getSizeOffset());
    reader.seek(pos);
    return reader.readInt32();
  }

  public void close() {
    try {
      this.file.close();
    } catch (final IOException e) {
      // nothing ..
    }
  }

}
