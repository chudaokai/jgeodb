package io.zrz.jgdb;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import io.zrz.jgdb.shape.GeometryValue;
import lombok.Builder;
import lombok.Value;

/**
 * @see https://github.com/rouault/dump_gdbtable/wiki/FGDB-Spec
 */

final class GeoDB_R5 extends AbstractGeoDB {

  // private static final int MIN_FEATURE_TABLE_ID = 0x3E;

  private static final int GDB_SystemCatalog_ID = 0x1;
  // private static final int GDB_DBTune_ID = 0x2;
  // private static final int GDB_SpatialRefs_ID = 0x3;
  // private static final int GDB_Release_ID = 0x4;
  // private static final int GDB_FeatureDataset_ID = 0x5;
  // private static final int GDB_ObjectClasses_ID = 0x6;
  // private static final int GDB_FeatureClasses_ID = 0x7;
  // private static final int GDB_FieldInfo_ID = 0x8;
  //
  // private static final int MIN_SYSTEM_TABLE = 36;

  private static final int TABLE_VERSION = 4;

  protected final Map<String, Long> catalog = new HashMap<>();

  private V10_Items items;

  GeoDB_R5(Path dir) {
    super(dir);
  }

  @Override
  protected void openCatalog() {

    // first, build the names and mappings.

    GeoTable index = this.openTable(GDB_SystemCatalog_ID, TABLE_VERSION);

    // fetch all of the non system tables.
    index.forEach((feature) -> {
      this.catalog.put(feature.getValue(0).stringValue(), feature.getFeatureId());
    });

    index.close();

    //
    // only if V10:
    this.items = new V10_Items(this);

  }

  @Override
  public GeoTable layer(String name) {
    Long fid = this.catalog.get(name);
    if (fid == null) {
      throw new IllegalArgumentException(name);
    }
    return GeoTable.open(this, fid);
  }

  public GeoTable getFeatureTableByLayerId(final int layer) {
    return null;
  }

  public List<String> getLayers() {
    return this.catalog.entrySet().stream()
        // .filter(e -> e.getValue() >= MIN_FEATURE_TABLE_ID)
        .filter(e -> this.tableExists(e.getValue()))
        .sorted((a, b) -> Long.compare(a.getValue(), b.getValue()))
        .map(e -> e.getKey())
        .collect(Collectors.toList());
  }

  public static GeoDB_R5 open(Path dir) {
    final GeoDB_R5 db = new GeoDB_R5(dir);
    db.open();
    return db;
  }

  @Override
  public FileGDBVersion getVersion() {
    return FileGDBVersion.V10;
  }

  @Value
  @Builder
  static class Item {
    private UUID uuid;
    private String type;
    private String physicalName;
    private String path;
    private int datasetSubtype1;
    private int datasetSubtype2;
    private String datasetInfo1;
    private String datasetInfo2;
    private int properties;
    private GeometryValue shape;
  }

  class V10_Items {

    private Map<String, Item> items = new HashMap<>();

    public V10_Items(AbstractGeoDB db) {

      try (GeoTable table = layer("GDB_Items")) {

        table.forEach(new RowConsumer() {

          @Override
          public void accept(GeoFeature feature) {

            if (!feature.getValue("Name").isNulled()) {

              Item.ItemBuilder ib = Item.builder();

              ib.uuid(feature.getValue("UUID").uuidValue());

              ib.type(feature.getValue("Type").stringValue());
              ib.physicalName(feature.getValue("PhysicalName").stringValue());

              if (!feature.getValue("Path").isNulled())
                ib.path(feature.getValue("Path").stringValue());

              ib.datasetSubtype1(feature.getValue("DatasetSubtype1").intValue());
              ib.datasetSubtype2(feature.getValue("DatasetSubtype2").intValue());
              ib.datasetInfo1(feature.getValue("DatasetInfo1").stringValue());
              ib.datasetInfo2(feature.getValue("DatasetInfo2").stringValue());

              ib.properties(feature.getValue("Properties").intValue());

              if (!feature.getValue("Shape").isNulled()) {
                ib.shape(feature.getValue("Shape").geometryValue());
              }

              items.put(feature.getValue("Name").stringValue(), ib.build());

            }

          }

        });

      }
    }

    public Item getItemByName(String name) {
      return items.get(name);
    }

  }
}
