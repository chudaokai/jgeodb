package io.zrz.jgdb;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Value;

/**
 * @see <a href='https://github.com/rouault/dump_gdbtable/wiki/FGDB-Spec'></a>
 */

final class GeoDB_R4 extends AbstractGeoDB {

  /**
   * The identifier for the system catalog table, the only one we reference by
   * ID rather than name.
   */

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

  private static final int MIN_FEATURE_TABLE_ID = 0x3E;

  private static final int TABLE_VERSION = 3;

  /**
   * The directory in both versions contains a system catalog which
   */

  protected final Map<String, CatalogItem> catalog = new HashMap<>();

  /**
   * The system tables.
   */

  private Map<String, CatalogItem> systemTables;

  /**
   * The user tables.
   */

  private Map<String, CatalogItem> userTables;

  @Value
  @Builder
  private static class CatalogItem {
    private String tableName;
    private long tableId;
    private int fileFormat;
    private UUID datasetGUID;
  }

  /**
   * 
   * @param dir
   */

  GeoDB_R4(Path dir) {
    super(dir);
  }

  /**
   * Opens a V9 catalog. In V9, this is
   * 
   * @see <a href='https://github.com/rouault/dump_gdbtable/wiki/FGDB-Spec'></a>
   */

  @Override
  protected void openCatalog() {

    // build the names and mappings.
    GeoTable index = this.openTable(GDB_SystemCatalog_ID, TABLE_VERSION);
    
    index.forEach((feature) -> {

      if (!this.tableExists(feature.getFeatureId())) {
        return;
      }

      this.catalog.put(
          feature.getValue(0).stringValue(),
          CatalogItem.builder()
              .tableName(feature.getValue(0).stringValue())
              .tableId(feature.getFeatureId())
              .fileFormat(feature.getValue(1).intValue())
              .datasetGUID(feature.getValue(2).uuidValue())
              .build());
    });

    index.close();

    this.systemTables = catalog.values().stream()
        .filter(e -> e.getTableId() < MIN_FEATURE_TABLE_ID)
        .collect(Collectors.toMap(a -> a.getTableName(), a -> a));

    this.userTables = catalog.values().stream()
        .filter(e -> e.getTableId() >= MIN_FEATURE_TABLE_ID)
        .collect(Collectors.toMap(a -> a.getTableName(), a -> a));

  }

  /**
   * Fetches the table by name.
   * 
   * @param name
   * @return
   */

  @Override
  public GeoTable layer(String name) {
    CatalogItem fid = this.userTables.get(name);
    if (fid == null) {
      throw new IllegalArgumentException(name);
    }
    return this.openTable(fid.getTableId(), TABLE_VERSION);
  }

  /**
   * (non-Javadoc)
   * 
   * @see io.zrz.jgdb.GeoDB#getLayers()
   */

  @Override
  public List<String> getLayers() {
    return getLayers(null);
  }

  @Override
  public List<String> getLayers(String datasetName) {
    return this.userTables.values().stream()
            .map(item -> item.getTableName())
            .collect(Collectors.toList());
  }

  @Override
  public Map<String, List<String>> getLayerTree() {
    return null;
  }

  @Override
  public List<String> getDatasets() {
    return getLayers();
  }

  @Override
  public FileGDBVersion getVersion() {
    return FileGDBVersion.V9;
  }

  // ---

  public static GeoDB_R4 open(Path dir) {
    final GeoDB_R4 db = new GeoDB_R4(dir);
    db.open();
    return db;
  }

}
