package io.zrz.jgdb;

import java.util.List;

/**
 * An instance of a GeoDB.
 * 
 * @author Theo Zourzouvillys
 *
 */

public interface GeoDB extends AutoCloseable {

  /**
   * The version that the underlying data is in. Seems there is currently V9
   * (binary representation 4) and V10 (binary representation 5).
   */

  VersionFormat getVersion();

  /**
   * Each of the layers.
   */

  List<String> getFeatureTables();

  /**
   * Fetches a feature table by it's name.
   * 
   * @param name
   * @return
   */

  GeoFeatureTable table(String name);

  /**
   * 
   */

  void close();

}
