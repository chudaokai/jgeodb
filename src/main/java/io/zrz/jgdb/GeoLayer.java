package io.zrz.jgdb;

import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a user-defined layer within the geodatabase.
 * 
 * @author Theo Zourzouvillys
 *
 */

public interface GeoLayer extends Iterable<GeoFeature> {

  /**
   * Fetches a feature by it's Feature ID / OBJECTID.
   * 
   * @param featureId
   *          The feature ID to fetch.
   * @return The feature.
   */

  GeoFeature getFeature(int featureId);

  /**
   * Fetches the number of features in this layer.
   */

  int getFeatureCount();

  /**
   * Fetch a specific field definition.
   */

  GeoField getField(String string);

  /**
   * The field which defines the shape of feature in this layer, if there is
   * one.
   */

  Optional<GeoField> getShapeField();

  /**
   * All of the fields defined for this table.
   */

  List<GeoField> getFields();

  void close();

  /**
   * Creates a {@link Spliterator} over the elements described by this
   * {@code Iterable}.
   *
   * @implSpec The default implementation creates an
   *           <em><a href="Spliterator.html#binding">early-binding</a></em>
   *           spliterator from the iterable's {@code Iterator}. The spliterator
   *           inherits the <em>fail-fast</em> properties of the iterable's
   *           iterator.
   *
   * @implNote The default implementation should usually be overridden. The
   *           spliterator returned by the default implementation has poor
   *           splitting capabilities, is unsized, and does not report any
   *           spliterator characteristics. Implementing classes can nearly
   *           always provide a better implementation.
   *
   * @return a {@code Spliterator} over the elements described by this
   *         {@code Iterable}.
   * @since 1.8
   */

  default Spliterator<GeoFeature> spliterator() {
    return Spliterators.spliteratorUnknownSize(iterator(), 0);
  }

  /**
   * Returns a sequential {@code Stream} with this collection as its source.
   *
   * <p>
   * This method should be overridden when the {@link #spliterator()} method
   * cannot return a spliterator that is {@code IMMUTABLE}, {@code CONCURRENT},
   * or <em>late-binding</em>. (See {@link #spliterator()} for details.)
   *
   * @implSpec The default implementation creates a sequential {@code Stream}
   *           from the collection's {@code Spliterator}.
   *
   * @return a sequential {@code Stream} over the elements in this collection
   * @since 1.8
   */
  default Stream<GeoFeature> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  /**
   * Returns a possibly parallel {@code Stream} with this collection as its
   * source. It is allowable for this method to return a sequential stream.
   *
   * <p>
   * This method should be overridden when the {@link #spliterator()} method
   * cannot return a spliterator that is {@code IMMUTABLE}, {@code CONCURRENT},
   * or <em>late-binding</em>. (See {@link #spliterator()} for details.)
   *
   * @implSpec The default implementation creates a parallel {@code Stream} from
   *           the collection's {@code Spliterator}.
   *
   * @return a possibly parallel {@code Stream} over the elements in this
   *         collection
   * @since 1.8
   */

  default Stream<GeoFeature> parallelStream() {
    return StreamSupport.stream(spliterator(), true);
  }

}
