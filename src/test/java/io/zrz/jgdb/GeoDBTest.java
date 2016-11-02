package io.zrz.jgdb;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;

import org.junit.Test;

// note: tests need unzipped FileGDBs in /tmp.

public class GeoDBTest {

  private void dumpTable(GeoDB db, String tableName) {

    System.err.println(tableName);

    GeoLayer table = db.layer(tableName);

    //System.err.println(String.format("  Table ID: 0x%x", table.getTableId()));
    System.err.println(String.format("  Feature Count: %d", table.getFeatureCount()));

    if (table.getShapeField().isPresent()) {

      GeometryFieldType geo = ((GeometryFieldType) table.getField("Shape").getType());

      System.err.println("  Layer SRS WKT: " + geo.getWkt());

      System.err.println(String.format("  Extent: (%.06f, %.06f) - (%.06f, %.06f)",
          geo.getXmin(), geo.getYmin(),
          geo.getXmax(), geo.getYmax()));

      System.err.println(String.format("  Origin: (%.06f, %.06f) scale=%.06f tolerance=%.06f",
          geo.getXorigin(), geo.getYorigin(),
          geo.getXyscale(), geo.getXytolerance()));

      System.err.println(String.format("  M: origin=%.06f scale=%.06f tolerance=%.06f",
          geo.getMorigin(), geo.getMscale(), geo.getMtolerance()));

      System.err.println(String.format("  Z: origin=%.06f scale=%.06f tolerance=%.06f",
          geo.getZorigin(), geo.getZscale(), geo.getZtolerance()));

    }

    for (GeoField field : table.getFields()) {
      if (field != table.getShapeField().orElse(null))
        System.err.println(String.format("  %s = %s", field.getName(), field.getType()));
    }

    table.forEach(row -> {

      System.err.println(String.format("  %s:%d", tableName, row.getFeatureId()));

      for (GeoField field : row.getFields()) {
        if (field != table.getShapeField().orElse(null))
          System.err.println(String.format("    %s = %s", field.getName(), row.getValue(field.getName())));
      }

      if (table.getShapeField().isPresent()) {
        System.err.println(String.format("    %s", row.getValue(table.getShapeField().get().getName())));
      }

      System.err.println();

    });

    System.err.println();

  }

  private void scanTable(GeoDB db, String tableName) {
    try {

      System.err.println(String.format("  Layer: %s", tableName));

      GeoLayer layer = db.layer(tableName);

//      System.err.println(String.format("  Table ID: 0x%x", layer.getTableId()));

      System.err.println(String.format("  Feature Count: %d", layer.getFeatureCount()));

      if (layer.getShapeField().isPresent()) {

        GeometryFieldType geo = ((GeometryFieldType) layer.getField("Shape").getType());

        System.err.println("  Layer SRS WKT: " + geo.getWkt());

        System.err.println(String.format("  Extent: (%.06f, %.06f) - (%.06f, %.06f)",
            geo.getXmin(), geo.getYmin(),
            geo.getXmax(), geo.getYmax()));

      }

      for (GeoField field : layer.getFields()) {
        if (field != layer.getShapeField().orElse(null))
          System.err.println(String.format("  %s = %s", field.getName(), field.getType()));
      }

      layer.forEach(row -> {

      });

      System.err.println();

    } catch (Exception ex) {

      throw new RuntimeException(String.format("While scanning '%s'", tableName), ex);

    }

  }

  @Test
  public void testUtils() {
    assertEquals("a00000001.gdbtable", AbstractGeoDB.makeFileName(1, "gdbtable"));
  }

  @Test
  public void testOpenV9() {

    final GeoDB db = FileGDBFactory.open(Paths.get("/tmp/UT_CadNSDI.gdb"));

    assertEquals(FileGDBVersion.V9, db.getVersion());

    // dumpTable(db, "MeanderedWater");

    dumpTable(db, "LU_PrincipalMeridian");

    for (String tableName : db.getLayers()) {
      scanTable(db, tableName);
    }

  }

  @Test
  public void testOpenV10_0() {
    final GeoDB db = FileGDBFactory.open(Paths.get("/tmp/UtahPLSS_Fabric_D5_FR_021916.gdb"));
    assertEquals(FileGDBVersion.V10, db.getVersion());
    dumpTable(db, "Utah_Parcel_Fabric_History");
  }

  @Test
  public void testOpenV10_1() {

    final GeoDB db = FileGDBFactory.open(Paths.get("/tmp/tlgdb_2015_a_us_school.gdb"));

    db.getLayers().forEach(System.err::println);

    assertEquals(FileGDBVersion.V10, db.getVersion());

    // scanTable(db, "School_District_Unified");

    for (String tableName : db.getLayers()) {
      scanTable(db, tableName);
    }
  }

  @Test
  public void testOpenV10_2() {

    final GeoDB db = FileGDBFactory.open(Paths.get("/tmp/UtahPLSS_Fabric_D5_FR_021916.gdb"));

    assertEquals(FileGDBVersion.V10, db.getVersion());

    // GeoFeatureTable table = db.table("Utah_Parcel_Fabric_Lines");

    // scanTable(db, "Utah_Parcel_Fabric_Control");
    // scanTable(db, "Utah_Parcel_Fabric_Points");
    // scanTable(db, "Utah_Parcel_Fabric_Parcels");
    // scanTable(db, "Utah_Parcel_Fabric_Lines");

    for (String tableName : db.getLayers()) {
      scanTable(db, tableName);
    }

  }

}
