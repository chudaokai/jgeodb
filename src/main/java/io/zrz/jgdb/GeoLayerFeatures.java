package io.zrz.jgdb;

/**
 * A resolved collection of features within a layer.
 * 
 * @author Theo Zourzouvillys
 *
 */

public class GeoLayerFeatures {

  private String name;
  private GeoFeatureTable table;

  public GeoLayerFeatures(String name, GeoFeatureTable table) {
    this.name = name;
    this.table = table;
  }

  public String getName() {
    return this.name;
  }

}
