package io.zrz.jgdb;

/**
 * A resolved collection of features within a layer.
 * 
 * @author Theo Zourzouvillys
 *
 */

public class GeoLayerFeatures {

  private String name;
  private GeoTable table;

  public GeoLayerFeatures(String name, GeoTable table) {
    this.name = name;
    this.table = table;
  }

  public String getName() {
    return this.name;
  }

}
