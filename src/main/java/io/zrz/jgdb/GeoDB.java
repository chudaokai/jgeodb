package io.zrz.jgdb;

import java.util.List;
import java.util.Map;

/**
 * An real-only instance of a GeoDB.
 * 
 * @author Theo Zourzouvillys <theo@zrz.io>
 *
 */

public interface GeoDB extends AutoCloseable {

  /**
   * The version that the underlying data is in. Seems there is currently V9
   * (binary representation 4) and V10 (binary representation 5).
   */

  FileGDBVersion getVersion();

  /**
   * Each of the layers.
   */

  List<String> getLayers();

  /**
   * List layers in a dataset.
   * @param datasetName name of the dataset.
   * @return
   */
  List<String> getLayers(String datasetName);

  Map<String,List<String>> getLayerTree();
  /**
   * Each of the datasets.
   * @return
   */
  List<String> getDatasets();

  /**
   * Fetches a layer by it's name.
   * 
   * A layer will be kept open until the database is closed. If you prefer to
   * close it to release resources, use GeoLayer#close().
   * 
   * The implementation uses mmap(), and very little (real) memory on a per
   * layer basis, so you most likely don't need to.
   * 
   */

  GeoLayer layer(String name);

  /**
   * 
   */

  void close();

}
