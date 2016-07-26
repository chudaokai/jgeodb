package io.zrz.jgdb;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;

import org.junit.Test;

import io.zrz.jgdb.GeoTableFile.Field;
import io.zrz.jgdb.GeoTableFile.GeometryFieldType;

public class GeoDBTest {

  private void dumpTable(GeoDB db, String tableName) {

    System.err.println(tableName);

    GeoFeatureTable table = db.table(tableName);

    System.err.println(String.format("  Table ID: 0x%x", table.getTableId()));
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

    for (Field field : table.getFields()) {
      if (field != table.getShapeField().orElse(null))
        System.err.println(String.format("  %s = %s", field.getName(), field.getType()));
    }

    table.scan(row -> {

      System.err.println(String.format("  %s:%d", tableName, row.getFeatureId()));

      for (Field field : row.getFields()) {
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

    System.err.println(String.format("  Layer: %s", tableName));

    GeoFeatureTable table = db.table(tableName);

    System.err.println(String.format("  Table ID: 0x%x", table.getTableId()));

    System.err.println(String.format("  Feature Count: %d", table.getFeatureCount()));

    if (table.getShapeField().isPresent()) {

      GeometryFieldType geo = ((GeometryFieldType) table.getField("Shape").getType());

      System.err.println("  Layer SRS WKT: " + geo.getWkt());

      System.err.println(String.format("  Extent: (%.06f, %.06f) - (%.06f, %.06f)",
          geo.getXmin(), geo.getYmin(),
          geo.getXmax(), geo.getYmax()));

    }

    for (Field field : table.getFields()) {
      if (field != table.getShapeField().orElse(null))
        System.err.println(String.format("  %s = %s", field.getName(), field.getType()));
    }

    table.scan(row -> {

    });

    System.err.println();

  }

  @Test
  public void testUtils() {
    assertEquals("a00000001.gdbtable", AbstractGeoDB.makeFileName(1, "gdbtable"));
  }

  @Test
  public void testOpenV9() {

    final GeoDB db = GeoDBFactory.open(Paths.get("/tmp/UT_CadNSDI.gdb"));

    assertEquals(VersionFormat.V9, db.getVersion());

    // dumpTable(db, "MeanderedWater");

    dumpTable(db, "LU_PrincipalMeridian");

    for (String tableName : db.getFeatureTables()) {
      scanTable(db, tableName);
    }

  }

  @Test
  public void testOpenV10_0() {
    final GeoDB db = GeoDBFactory.open(Paths.get("/tmp/UtahPLSS_Fabric_D5_FR_021916.gdb"));
    assertEquals(VersionFormat.V10, db.getVersion());
    dumpTable(db, "Utah_Parcel_Fabric_History");
  }

  @Test
  public void testOpenV10_1() {

    final GeoDB db = GeoDBFactory.open(Paths.get("/tmp/tlgdb_2015_a_us_school.gdb"));

    db.getFeatureTables().forEach(System.err::println);

    assertEquals(VersionFormat.V10, db.getVersion());

    // scanTable(db, "School_District_Unified");

    for (String tableName : db.getFeatureTables()) {
      scanTable(db, tableName);
    }
  }

  @Test
  public void testOpenV10_2() {

    final GeoDB db = GeoDBFactory.open(Paths.get("/tmp/UtahPLSS_Fabric_D5_FR_021916.gdb"));
    assertEquals(VersionFormat.V10, db.getVersion());

    // scanTable(db, "Utah_Parcel_Fabric_Control");
    // dumpTable(db, "Utah_Parcel_Fabric_Points");
    // dumpTable(db, "Utah_Parcel_Fabric_Parcels");
    // dumpTable(db, "Utah_Parcel_Fabric_Lines");

    for (String tableName : db.getFeatureTables()) {
      scanTable(db, tableName);
    }

  }

}
