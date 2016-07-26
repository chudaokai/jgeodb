package io.zrz.jgdb;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface GeoLayer {

  /**
   * Fetches a feature by it's Feature ID / OBJECTID.
   * 
   * @param featureId
   *          The feature ID to fetch.
   * @return The feature.
   */

  GeoFeature getFeature(int featureId);

  int getFeatureCount();

  GeoField getField(String string);

  Optional<GeoField> getShapeField();

  int getTableId();

  List<GeoField> getFields();

  void forEach(final Predicate<Long> acceptor, final RowConsumer feature);

  default void forEach(final RowConsumer feature) {
    forEach(rowid -> true, feature);
  }

  void close();

}
