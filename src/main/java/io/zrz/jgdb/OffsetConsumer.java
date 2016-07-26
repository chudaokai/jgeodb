package io.zrz.jgdb;

@FunctionalInterface

public interface OffsetConsumer {

  void accept(long objid, long offset);

}
