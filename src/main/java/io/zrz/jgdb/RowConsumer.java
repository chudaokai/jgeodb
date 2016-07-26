package io.zrz.jgdb;

@FunctionalInterface

public interface RowConsumer {

  void accept(GeoFeature values);

}
