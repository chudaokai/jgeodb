package io.zrz.jgdb;

import java.io.IOException;

import io.zrz.jgdb.shape.GeometryValue;
import io.zrz.jgdb.shape.MultiPoint;
import io.zrz.jgdb.shape.Point;
import io.zrz.jgdb.shape.PointValue;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GeometryFieldType implements FieldType {

  private boolean nullable;

  private String wkt;
  private boolean hasZ, hasM, has3D;

  private double xorigin, yorigin, xyscale;

  private double morigin, mscale;
  private double zorigin, zscale;

  private double xytolerance;
  private double mtolerance, ztolerance;

  private double xmin, ymin, xmax, ymax;

  /**
   * 
   */

  @Override
  public Object read(GeoBuffer file) throws IOException {

    // the length.
    int len = file.readVarUInt32();

    // the current position
    long pos = file.getFilePointer();

    // the geometry type.
    int type = file.readVarUInt32();

    //

    try {

      ShapeModifiers stype = ShapeModifiers.fromValue(type);

      switch (stype.getStructure()) {

        case ShapeNull:
          // a null shape?
          return null;

        case ShapePoint:
        case ShapePointZ:
        case ShapePointM:
        case ShapePointZM:
        case ShapeGeneralPoint:
          return parsePoint(stype, file, len - (file.getFilePointer() - pos));

        case ShapePolygon:
        case ShapePolygonZ:
        case ShapePolygonM:
        case ShapePolygonZM:
        case ShapeGeneralPolygon:
          return parsePolygon(stype, file, len - (file.getFilePointer() - pos));

        case ShapeMultipoint:
        case ShapeMultipointM:
        case ShapeMultipointZ:
        case ShapeMultipointZM:
        case ShapeGeneralMultipoint:
          break;

        case ShapePolyline:
        case ShapePolylineM:
        case ShapePolylineZ:
        case ShapePolylineZM:
        case ShapeGeneralPolyline:
          return parsePolyline(stype, file, len - (file.getFilePointer() - pos));

        // unsupported for now.
        case ShapeGeneralMultiPatch:
        default:
          break;
      }

      throw new GeoDBException(String.format("Unsupported shape type '%s'", stype.getStructure()));

    } finally {
      file.seek(pos + len);
    }

  }

  private PointValue parsePoint(ShapeModifiers stype, GeoBuffer buffer, long len) {

    // GeoBuffer buffer = new GeoFileBuffer(cfile, len);

    double x = ((buffer.readVarUInt64() - 1) / this.xyscale) + this.xorigin;
    double y = ((buffer.readVarUInt64() - 1) / this.xyscale) + this.yorigin;

    if (stype.hasZ()) {

      double z = (buffer.readVarUInt64() - 1);

      if (z != 0.00) {
        z = (z / this.zscale) + this.zorigin;
      }

      if (stype.hasM()) {
        double m = (buffer.readVarUInt64() / this.mscale) + this.morigin;
        return new PointValue(x, y, z, m);
      }

      return new PointValue(x, y, z, Double.NaN);

    }

    if (stype.hasM()) {
      double m = (buffer.readVarUInt64() / this.mscale) + this.morigin;
      return new PointValue(x, y, Double.NaN, m);
    }

    return new PointValue(x, y, Double.NaN, Double.NaN);

  }

  private GeometryValue parsePolyline(ShapeModifiers stype, GeoBuffer cfile, long len) throws IOException {
    // polylines are actually identical to polygons in structure.
    return parsePolygon(stype, cfile, len);
  }

  /**
   * 
   */

  private GeometryValue parsePolygon(ShapeModifiers stype, GeoBuffer buffer, long len) throws IOException {

    // GeoBuffer buffer = new GeoFileBuffer(cfile, len);

    int npoints = buffer.readVarUInt32();

    if (npoints < 0 || npoints > (50 * 1000 * 1000)) {
      throw new IllegalArgumentException();
    } else if (npoints == 0) {
      return null;
    }

    // varuint: number of parts, i.e. number of rings for (multi)polygon -
    // inner and outer rings being at the same level, number of linestrings of
    // a multilinestring, or 1 for a linestring)
    int ngeoms = buffer.readVarUInt32();

    // if we have curves, this is the number.
    final int ncurves = (stype.hasCurves()) ? buffer.readVarUInt32() : 0;

    if (ncurves < 0 || ncurves > npoints) {
      throw new IllegalArgumentException();
    }

    // -- the bounding box for the feature.

    // varuint: xmin = varuint / xyscale + xorigin
    double xmin = buffer.readVarUInt64() / xyscale + xorigin;

    // varuint: ymin = varuint / xyscale + yorigin
    double ymin = buffer.readVarUInt64() / xyscale + yorigin;

    // varuint: xmax = varuint / xyscale + xmin
    double xmax = buffer.readVarUInt64() / xyscale + xorigin;

    // varuint: ymax = varuint / xyscale + ymin
    double ymax = buffer.readVarUInt64() / xyscale + yorigin;

    MultiPoint parts = new MultiPoint();
    parts.points = new Point[ngeoms];
    int remain = npoints;

    //

    for (int i = 0; i < (ngeoms - 1); ++i) {

      int pointsperpart = (int) buffer.readVarUInt64();

      remain -= pointsperpart;

      if (remain < 0) {
        throw new GeoDBException(String.format("Invalid number of points: %d", remain));
      }

      parts.points[i] = new Point();
      parts.points[i].x = new double[pointsperpart];
      parts.points[i].y = new double[pointsperpart];

      if (stype.hasZ()) {
        parts.points[i].z = new double[pointsperpart];
      }

      if (stype.hasM()) {
        parts.points[i].m = new double[pointsperpart];
      }

    }

    parts.points[ngeoms - 1] = new Point();
    parts.points[ngeoms - 1].x = new double[remain];
    parts.points[ngeoms - 1].y = new double[remain];

    if (stype.hasZ()) {
      parts.points[ngeoms - 1].z = new double[remain];
    }

    if (stype.hasM()) {
      parts.points[ngeoms - 1].m = new double[remain];
    }

    // --

    long dx = 0;
    long dy = 0;

    for (int i = 0; i < ngeoms; ++i) {

      Point point = parts.points[i];

      for (int x = 0; x < point.x.length; ++x) {

        long vi = buffer.readVarInt64();
        dx += vi;

        vi = buffer.readVarInt64();
        dy += vi;

        point.x[x] = (dx / this.xyscale) + this.xorigin;
        point.y[x] = (dy / this.xyscale) + this.yorigin;

      }

    }

    // ---

    if (stype.hasZ()) {
      long dz = 0;
      for (int i = 0; i < ngeoms; ++i) {
        Point point = parts.points[i];
        for (int x = 0; x < point.x.length; ++x) {
          long vi = buffer.readVarInt64();
          dz += vi;
          point.z[x] = (dz / this.zscale) + this.zorigin;
        }
      }
    }

    if (stype.hasM()) {
      long dm = 0;
      for (int i = 0; i < ngeoms; ++i) {
        Point point = parts.points[i];
        for (int x = 0; x < point.x.length; ++x) {
          long vi = buffer.readVarInt64();
          dm += vi;
          point.m[x] = (dm / this.mscale) + this.morigin;
        }
      }
    }

    for (int c = 0; c < ncurves; ++c) {

      long idx = buffer.readVarInt64();
      int type = buffer.readVarUInt32();

      switch (type) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        default:
          throw new IllegalArgumentException("Unknown Curve Type " + type);
      }

    }

    return parts;

  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

  @Override
  public <R> R apply(Object object, GeoValueVisitor<R> converter) {
    return converter.visitGeometry((GeometryValue) object);
  }

}