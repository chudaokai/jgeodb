package io.zrz.jgdb.example;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;

import io.zrz.jgdb.GeoDB;
import io.zrz.jgdb.FileGDBFactory;
import io.zrz.jgdb.GeoLayer;
import io.zrz.jgdb.FileGDBVersion;

public class ExportLines {

  public static void main(String[] args) {

    final GeoDB db = FileGDBFactory.open(Paths.get("/tmp/UtahPLSS_Fabric_D5_FR_021916.gdb"));

    assertEquals(FileGDBVersion.V10, db.getVersion());

    GeoLayer layer = db.layer("Utah_Parcel_Fabric_Lines");

    layer.forEach(row -> {
      
      System.err.println(row.getValue(""));
      
    });
    
  }

}
