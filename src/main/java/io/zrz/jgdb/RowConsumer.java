package io.zrz.jgdb;

import java.util.function.Consumer;

@FunctionalInterface

public interface RowConsumer extends Consumer<GeoFeature> {

  void accept(GeoFeature values);

}
