package io.zrz.jgdb;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class ShapeModifiers {

  private int type;
  private ShapeType shape;

  private ShapeModifiers(int flags) {
    this.type = flags;
    this.shape = ShapeType.fromValue(flags & 255);
  }

  public static ShapeModifiers fromValue(int type) {
    return new ShapeModifiers(type);
  }

  public boolean hasZ() {
    switch (this.getStructure()) {
      case ShapePointZ:
      case ShapePointZM:
      case ShapePolygonZ:
      case ShapePolygonZM:
      case ShapeMultipointZ:
      case ShapeMultipointZM:
      case ShapePolylineZ:
      case ShapePolylineM:
        return true;
      default:
        return (type & 0x80000000) != 0;
    }
  }

  public boolean hasM() {
    switch (this.getStructure()) {
      case ShapePointM:
      case ShapePointZM:
      case ShapePolygonM:
      case ShapePolygonZM:
      case ShapeMultipointM:
      case ShapeMultipointZM:
      case ShapePolylineM:
      case ShapePolylineZM:
        return true;
      default:
        return (type & 0x40000000) != 0;
    }
  }

  public boolean hasCurves() {

    switch (this.getStructure()) {
      case ShapeGeneralPolyline:
      case ShapeGeneralPolygon:
        if (!this.hasModifiers())
          return true;
        break;
      default:
        break;
    }

    return (type & 0x20000000) != 0;

  }

  public boolean hasIDs() {
    return (type & 0x10000000) != 0;
  }

  public boolean hasNormals() {
    return (type & 0x08000000) != 0;
  }

  public boolean hasTextures() {
    return (type & 0x04000000) != 0;
  }

  public boolean hasPartIDs() {
    return (type & 0x02000000) != 0;
  }

  public boolean hasMaterials() {
    return (type & 0x01000000) != 0;
  }

  public ShapeType getStructure() {
    return this.shape;
  }

  public String toString() {

    // magic flag for "no modifiers"
    if (!this.hasModifiers()) {
      return "[" + this.getStructure() + "]";
    }

    List<String> parts = new ArrayList<>();

    if (hasZ())
      parts.add("Z");
    if (hasM())
      parts.add("M");
    if (hasCurves())
      parts.add("CURVE");
    if (hasIDs())
      parts.add("ID");
    if (hasNormals())
      parts.add("NORMAL");
    if (hasTextures())
      parts.add("TEXTURE");
    if (hasPartIDs())
      parts.add("PARTID");
    if (hasMaterials())
      parts.add("MATERIAL");

    if (parts.size() == 0) {
    }

    return "[" + this.getStructure() + "(" + parts.stream().collect(Collectors.joining(",")) + ")]";

  }

  private boolean hasModifiers() {
    return (this.type & 1056964608) != 0;
  }

}
