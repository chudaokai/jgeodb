package io.zrz.jgdb;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;


// note: tests need unzipped FileGDBs in /tmp.

public class GeoDBTest {

  private void dumpTable(GeoDB db, String tableName) {

    System.err.println(tableName);

    GeoLayer table = db.layer(tableName);

    //System.err.println(String.format("  Table ID: 0x%x", table.getTableId()));
    System.err.println(String.format("  Feature Count: %d", table.getFeatureCount()));

    if (table.hasGeometry()) {

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
      if (field != table.getShapeField())
        System.err.println(String.format("  %s = %s", field.getName(), field.getType()));
    }

    table.forEach(row -> {

      System.err.println(String.format("  %s:%d", tableName, row.getFeatureId()));

      for (GeoField field : row.getFields()) {
        if (field != table.getShapeField())
          System.err.println(String.format("    %s = %s", field.getName(), row.getValue(field.getName())));
      }

      if (table.hasGeometry()) {
        System.err.println(String.format("    %s", row.getValue(table.getShapeField().getName())));
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

      if (layer.hasGeometry()) {

        GeometryFieldType geo = ((GeometryFieldType) layer.getField("Shape").getType());

        System.err.println("  Layer SRS WKT: " + geo.getWkt());

        System.err.println(String.format("  Extent: (%.06f, %.06f) - (%.06f, %.06f)",
            geo.getXmin(), geo.getYmin(),
            geo.getXmax(), geo.getYmax()));

      }

      for (GeoField field : layer.getFields()) {
        if (field != layer.getShapeField())
          System.err.println(String.format("  %s = %s", field.getName(), field.getType()));
      }

      layer.forEach(row -> {
        System.err.println(String.format("  %s:%d", tableName, row.getFeatureId()));

        for (GeoField field : row.getFields()) {
          if (field != layer.getShapeField())
            System.err.println(String.format("    %s = %s", field.getName(), row.getValue(field.getName())));
        }

        if (layer.hasGeometry()) {
          System.err.println(String.format("    %s", row.getValue(layer.getShapeField().getName())));
        }

        System.err.println();
      });

      System.err.println();

    } catch (Exception ex) {

      throw new RuntimeException(String.format("While scanning '%s'", tableName), ex);

    }

  }

  public void testUtils() {
    assertEquals("a00000001.gdbtable", AbstractGeoDB.makeFileName(1, "gdbtable"));
  }

  public void testOpenV9() {

    final GeoDB db = FileGDBFactory.open(Paths.get("/tmp/UT_CadNSDI.gdb"));

    assertEquals(FileGDBVersion.V9, db.getVersion());

    // dumpTable(db, "MeanderedWater");

    dumpTable(db, "LU_PrincipalMeridian");

    for (String tableName : db.getLayers()) {
      scanTable(db, tableName);
    }

  }

  public void testOpenV10_0() {
    final GeoDB db = FileGDBFactory.open(Paths.get("E:\\tmp\\test.gdb"));
    assertEquals(FileGDBVersion.V10, db.getVersion());
    dumpTable(db, "test1");
  }

  public void testOpenV10_1() {

    final GeoDB db = FileGDBFactory.open(Paths.get("E:\\temp\\≤‚ ‘(2)\\ceshi.gdb"));

    db.getLayers().forEach(System.err::println);

    assertEquals(FileGDBVersion.V10, db.getVersion());

    // scanTable(db, "School_District_Unified");

    for (String tableName : db.getLayers()) {
      scanTable(db, tableName);
    }
  }

}
