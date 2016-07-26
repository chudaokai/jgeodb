package io.zrz.jgdb;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum ShapeType {

  ShapeNull(0),

  // polylines

  ShapePolyline(3),

  ShapePolylineM(23),

  ShapePolylineZM(13),

  ShapePolylineZ(10),

  ShapeGeneralPolyline(50),


  // polygons

  ShapePolygon(5),

  ShapePolygonM(25),

  ShapePolygonZM(15),

  ShapePolygonZ(19),

  ShapeGeneralPolygon(51),

  // points

  ShapePoint(1),

  ShapePointM(21),

  ShapePointZM(11),

  ShapePointZ(9),

  ShapeGeneralPoint(52),

  // multi point


  ShapeMultipoint(8),

  ShapeMultipointM(28),

  ShapeMultipointZM(18),

  ShapeMultipointZ(20),

  ShapeGeneralMultipoint(53),

  // general shapes

  ShapeGeneralMultiPatch(54),

  ;

  private int value;

  private ShapeType(int val) {
    this.value = val;
  }

  public boolean isGeneral() {
    return this.value >= 50;
  }

  public static ShapeType fromValue(int structure) {
    return Objects.requireNonNull(lookup.get(structure), String.format("Unknown Shape ID '%d'", structure));
  }

  private static Map<Integer, ShapeType> lookup = new HashMap<>();

  static {
    for (ShapeType shape : ShapeType.values()) {
      lookup.put(shape.value, shape);
    }
  }

}
