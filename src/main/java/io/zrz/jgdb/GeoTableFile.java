package io.zrz.jgdb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import io.zrz.jgdb.GeometryValue.PointValue;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

public class GeoTableFile {

  @Value
  @Builder
  private static class StringFieldType implements FieldType {

    private boolean nullable;
    private int maxlen;
    private String defaultValue;

    @Override
    public Object read(GeoBuffer file) throws IOException {

      int clen = file.readVarUInt32();

      if (clen == 0) {
        return null;
      }

      byte data[] = new byte[clen];
      file.readFully(data);

      String str = new String(data);
      return str;

    }

    @Override
    public Object getDefaultValue() {
      return defaultValue;
    }

  }

  @Value
  @Builder
  private static class XmlFieldType implements FieldType {

    private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder builder;

    static {
      try {
        builder = factory.newDocumentBuilder();
      } catch (ParserConfigurationException e) {
        throw new RuntimeException(e);
      }
    }

    private boolean nullable;
    // private int maxlen;
    private String defaultValue;

    @Override
    public Object read(GeoBuffer file) throws IOException {

      int len = file.readVarUInt32();

      byte data[] = new byte[len];

      file.readFully(data);

      Document doc;
      try {
        doc = builder.parse(new ByteArrayInputStream(data));
      } catch (SAXException e) {
        throw new GeoDBException(e);
      }

      return doc;
    }

    @Override
    public Object getDefaultValue() {
      return defaultValue;
    }

  }

  @Value
  @Builder
  private static class BinaryFieldType implements FieldType {

    private boolean nullable;

    @Override
    public Object read(GeoBuffer file) throws IOException {
      int len = file.readVarUInt32();
      byte data[] = new byte[len];
      file.readFully(data);
      return data;
    }

    @Override
    public Object getDefaultValue() {
      return null;
    }

  }

  @Value
  @Builder
  private static class IntFieldType implements FieldType {
    private boolean nullable;
    private int width;
    private long defaultValue;

    @Override
    public Object read(GeoBuffer file) throws IOException {
      return file.readInt32();
    }

    @Override
    public Object getDefaultValue() {
      return defaultValue;
    }

  }

  @Value
  @Builder
  private static class ShortFieldType implements FieldType {

    private boolean nullable;
    private int width;
    private long defaultValue;

    @Override
    public Object read(GeoBuffer file) throws IOException {
      return file.readInt16();
    }

    @Override
    public Object getDefaultValue() {
      return defaultValue;
    }

  }

  @Value
  @Builder
  private static class FloatFieldType implements FieldType {

    private boolean nullable;
    private int width;
    private long defaultValue;

    @Override
    public Object read(GeoBuffer file) throws IOException {
      return file.readF32();
    }

    @Override
    public Object getDefaultValue() {
      return defaultValue;
    }

  }

  @Value
  @Builder
  private static class DoubleFieldType implements FieldType {

    private boolean nullable;
    private int width;
    private long defaultValue;

    @Override
    public Object read(GeoBuffer file) throws IOException {
      return file.readD64();
    }

    @Override
    public Object getDefaultValue() {
      return defaultValue;
    }

  }

  private static Instant EPOCH = Instant.parse("1899-12-30T00:00:00.00Z");

  @Value
  @Builder
  private static class DateFieldType implements FieldType {

    private boolean nullable;
    private int width;
    private long defaultValue;

    @Override
    public Object read(GeoBuffer file) throws IOException {
      double date = file.readD64();

      // print('Field %s : %f days since 1899/12/30' % (fields[ifield].name,
      // val))

      return EPOCH.plusSeconds((long) Math.ceil(date * (3600.0 * 24.0)));

    }

    @Override
    public Object getDefaultValue() {
      return defaultValue;
    }

  }

  @ToString
  private static class ObjectIdFieldType implements FieldType {

    public static final ObjectIdFieldType INSTANCE = new ObjectIdFieldType();

    /**
     * Can't read the object ID field ...
     */

    @Override
    public Object read(GeoBuffer file) throws IOException {
      throw new RuntimeException("objid");
    }

    @Override
    public boolean isNullable() {
      return false;
    }

    @Override
    public Object getDefaultValue() {
      return null;
    }

  }

  @ToString
  public static class UUIDFieldType implements FieldType {

    public static final UUIDFieldType NULLABLE_INSTANCE = new UUIDFieldType(true);
    public static final UUIDFieldType INSTANCE = new UUIDFieldType(false);
    private boolean nullable;

    public UUIDFieldType(boolean nullable) {
      this.nullable = nullable;
    }

    @Override
    public Object read(GeoBuffer file) throws IOException {
      byte b[] = new byte[16];
      file.readFully(b);
      return UUID.fromString(
          String.format("%02X%02X%02X%02X-%02X%02X-%02X%02X-%02X%02X-%02X%02X%02X%02X%02X%02X", b[3], b[2], b[1], b[0], b[5], b[4], b[7], b[6], b[8], b[9], b[10], b[11],
              b[12], b[13], b[14], b[15]));
    }

    @Override
    public boolean isNullable() {
      return nullable;
    }

    @Override
    public Object getDefaultValue() {
      return null;
    }

  }

  @Value
  @Builder
  public static class GeometryFieldType implements FieldType {

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

    private GeometryValue.PointValue parsePoint(ShapeModifiers stype, GeoBuffer buffer, long len) {

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

      // varuint: xmin = varuint / xyscale + xorigin
      double xmin = buffer.readVarUInt64() / xyscale + xorigin;

      // varuint: ymin = varuint / xyscale + yorigin
      double ymin = buffer.readVarUInt64() / xyscale + yorigin;

      // varuint: xmax = varuint / xyscale + xmin
      double xmax = buffer.readVarUInt64() / xyscale + xorigin;

      // varuint: ymax = varuint / xyscale + ymin
      double ymax = buffer.readVarUInt64() / xyscale + yorigin;

      //

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

  }

  public static class MultiPoint implements GeometryValue {
    public Point points[];

    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("MultiPoint(");
      for (int i = 0; i < points.length; ++i) {
        if (i > 0)
          sb.append("\n, ");
        sb.append("(").append(points[i].toString()).append(")");
      }
      sb.append(")");
      return sb.toString();
    }

    @Override
    public void visit(GeometryValueVisitor visitor) {
      visitor.visitMultipoint(this);
    }

  }

  public static class Point {

    double x[];
    double y[];
    double z[];
    double m[];

    public String toString() {

      StringBuilder sb = new StringBuilder();

      sb.append("(");

      for (int i = 0; i < x.length; ++i) {
        if (i > 0)
          sb.append(", ");
        sb.append(x[i]).append(" ").append(y[i]);
      }

      sb.append(")");

      return sb.toString();

    }

  }

  private interface FieldType {

    boolean isNullable();

    Object read(GeoBuffer file) throws IOException;

    Object getDefaultValue();

  }

  @Value
  @Builder
  public static class Field {
    private final String name;
    private final String alias;
    private final FieldType type;
  }

  @Builder
  @Value
  private static class Header {

    // int32: number of (valid) rows
    long rows;
    int filesize;
    int offset;

    int bytes;
    int version;
    int type;

    // 4 bytes: varying values - unknown role (TBC : this value does have
    // something to do with row size. A value larger than the size of the
    // largest row seems to be ok)

    // 4 bytes: 0x05 0x00 0x00 0x00 - unknown role. Constant among the files

    // 4 bytes: varying values - unknown role. Seems to be 0x00 0x00 0x00 0x00
    // for FGDB 10 files, but not for earlier versions
    // 4 bytes: 0x00 0x00 0x00 0x00 - unknown role. Constant among the files
    // int32: file size in bytes

    // 4 bytes: 0x00 0x00 0x00 0x00 - unknown role. Constant among the files
    // int32: offset in bytes at which the field description section begins
    // (often 40 in FGDB 10)

    // 4 bytes: 0x00 0x00 0x00 0x00 - unknown role. Constant among the files

  }

  private final RandomAccessFile xfile;
  private Header header;
  private List<Field> fields;

  /**
   * The IDs for the two "system" fields.
   */

  private Integer fidId;
  private Integer shapeId;
  private GeoMappedFileBuffer file;

  /**
   * 
   * @param path
   * @throws IOException
   */

  public GeoTableFile(final Path path) throws IOException {
    this.xfile = new RandomAccessFile(path.toFile(), "r");
    this.file = new GeoMappedFileBuffer(xfile, xfile.length());
    this.header = this.readHeader();
    this.fields = this.readFields();
  }

  public Header readHeader() throws IOException {

    final int magic = file.readInt();

    if (magic != 0x3000000) {
      throw new IllegalArgumentException("Invalid Magic");
    }

    //

    final Header.HeaderBuilder b = Header.builder();

    //
    b.rows(Integer.reverseBytes(file.readInt()));
    file.skipBytes(16);

    b.filesize(Integer.reverseBytes(file.readInt()));
    file.skipBytes(4);

    int offset = Integer.reverseBytes(file.readInt());

    b.offset(offset);

    file.seek(offset);

    final int bytes = Integer.reverseBytes(file.readInt());
    final int version = Integer.reverseBytes(file.readInt());
    int geotype = file.readUnsignedByte();

    b.bytes(bytes);
    b.version(version);
    b.type(geotype);

    file.skipBytes(3);

    // now, check values.

    if (bytes < 10 || bytes > 65535) {
      throw new IllegalArgumentException(String.format("Invalid fields section size: %d", bytes));
    }

    if (version != 4 && version != 3) {
      throw new IllegalArgumentException(String.format("Only support version 9 or 10 (byte value 3 or 4), got '%d'", version));
    }

    switch (geotype) {
      case 0:
        // none
      case 1:
        // point
      case 2:
        // multipoint
      case 3:
        // (multi)polyline
      case 4:
        // (multi)polygon
      case 9:
        // multipatch
        break;
      default:
        throw new IllegalArgumentException(String.format("Unknown geotype '%s'", geotype));
    }

    return b.build();

  }

  public List<Field> readFields() throws IOException {

    int fields = file.readInt16();

    if (fields < 0 || fields > 65535) {
      throw new IllegalArgumentException(String.format("Invalid fields count: %d", fields));
    }

    //
    List<Field> ret = new ArrayList<>();

    for (int i = 0; i < fields; ++i) {

      Field field = readField();

      if (field.getType() == ObjectIdFieldType.INSTANCE) {
        continue;
      }

      if (field.getType() instanceof GeometryFieldType) {
        this.shapeId = i;
      }

      ret.add(field);

    }

    return ret;

  }

  private Field readField() throws IOException {

    Field.FieldBuilder b = Field.builder();

    String name = readUTFString();
    b.name(name);

    String alias = readUTFString();
    b.alias(alias);

    int geotype = file.readUnsignedByte();

    switch (geotype) {
      case 5:
      case 3:
      case 2:
      case 1:
      case 0: {

        int width = file.readUnsignedByte();
        int flags = file.readUnsignedByte();

        // the length of the default value ...
        int defaultLength = file.readUnsignedByte();

        if (defaultLength != 0) {

          if (defaultLength != width) {
            throw new GeoDBException("Not implemented");
          }

          // TODO:
          file.skipBytes(defaultLength);

        }

        if ((flags & 4) != 0) {

          // a default value.

        }

        switch (geotype) {
          case 0: {
            ShortFieldType.ShortFieldTypeBuilder ib = ShortFieldType.builder();
            ib.width(width);
            ib.nullable((flags & 1) == 1);
            b.type(ib.build());
            ib.defaultValue(0);
            break;
          }
          case 1: {
            IntFieldType.IntFieldTypeBuilder ib = IntFieldType.builder();
            ib.width(width);
            ib.nullable((flags & 1) == 1);
            b.type(ib.build());
            ib.defaultValue(0);
            break;
          }
          case 2: {
            FloatFieldType.FloatFieldTypeBuilder ib = FloatFieldType.builder();
            ib.width(width);
            ib.nullable((flags & 1) == 1);
            b.type(ib.build());
            ib.defaultValue(0);
            break;
          }
          case 3: {
            DoubleFieldType.DoubleFieldTypeBuilder ib = DoubleFieldType.builder();
            ib.width(width);
            ib.nullable((flags & 1) == 1);
            b.type(ib.build());
            ib.defaultValue(0);
            break;
          }
          case 5: {
            DateFieldType.DateFieldTypeBuilder ib = DateFieldType.builder();
            ib.width(width);
            ib.nullable((flags & 1) == 1);
            b.type(ib.build());
            ib.defaultValue(0);
            break;
          }
          default:
            throw new RuntimeException();
        }

        break;
      }
      case 4: {

        StringFieldType.StringFieldTypeBuilder sb = StringFieldType.builder();

        // string
        int maxlen = Integer.reverseBytes(file.readInt());

        sb.maxlen(maxlen);

        int flags = file.readUnsignedByte();

        int defaultLength = readUnsignedVarInt();

        if ((flags & 4) != 0) {
          byte[] defaultValue = new byte[defaultLength];
          file.readFully(defaultValue);
          sb.defaultValue(new String(defaultValue, StandardCharsets.UTF_8));
        }

        sb.nullable((flags & 1) == 1);

        b.type(sb.build());

        break;
      }
      case 6:
        // objectid
        file.skipBytes(2);
        b.type(ObjectIdFieldType.INSTANCE);
        break;
      case 10:
      case 11: {
        // uuid
        file.skipBytes(1);
        int flags = file.readUnsignedByte();
        b.type((flags & 1) == 1 ? UUIDFieldType.NULLABLE_INSTANCE : UUIDFieldType.INSTANCE);
        break;
      }
      case 12: {
        // XML
        file.skipBytes(1);
        int flags = file.readUnsignedByte();
        XmlFieldType.XmlFieldTypeBuilder sb = XmlFieldType.builder();
        sb.nullable((flags & 1) == 1);
        b.type(sb.build());
        break;
      }
      case 8: {
        // binary
        file.skipBytes(1);
        int flags = file.readUnsignedByte();
        BinaryFieldType.BinaryFieldTypeBuilder sb = BinaryFieldType.builder();
        sb.nullable((flags & 1) == 1);
        b.type(sb.build());
        break;
      }
      case 7: {

        GeometryFieldType.GeometryFieldTypeBuilder sb = GeometryFieldType.builder();

        // geometry
        file.skipBytes(1);

        int flag = file.readUnsignedByte();

        sb.nullable((flag & 1) == 1);

        int wktlen = file.readInt16();

        if (wktlen > 0) {

          byte[] data = new byte[wktlen];
          file.readFully(data);
          String wkt = new String(data, StandardCharsets.UTF_8);
          sb.wkt(wkt);
        }

        int flags = file.readUnsignedByte();

        // int has3d

        boolean hasM = (flags & 0x2) != 0;
        boolean hasZ = (flags & 0x4) != 0;

        sb.hasZ(hasZ);
        sb.hasM(hasM);

        // float64: xorigin
        sb.xorigin(file.readD64());

        // float64: yorigin
        sb.yorigin(file.readD64());

        // float64: xyscale
        sb.xyscale(file.readD64());

        if (hasM) {

          // float64: morigin (present only if has_m = True)
          sb.morigin(file.readD64());

          // float64: mscale (present only if has_m = True)
          sb.mscale(file.readD64());

        }

        if (hasZ) {

          // float64: zorigin (present only if has_z = True)
          sb.zorigin(file.readD64());

          // float64: zscale (present only if has_z = True)
          sb.zscale(file.readD64());

        }

        // float64: xytolerance
        sb.xytolerance(file.readD64());

        if (hasM) {
          // float64: mtolerance (present only if has_m = True)
          sb.mtolerance(file.readD64());
        }

        if (hasZ) {
          // float64: ztolerance (present only if has_z = True)
          sb.ztolerance(file.readD64());
        }

        // float64: xmin of layer extent (might be NaN)
        sb.xmin(file.readD64());

        // float64: ymin of layer extent (might be NaN)
        sb.ymin(file.readD64());

        // float64: xmax of layer extent (might be NaN)
        sb.xmax(file.readD64());

        // float64: ymax of layer extent (might be NaN)
        sb.ymax(file.readD64());

        // Read 5 bytes. no idea what they are for atm.

        // TODO: work this out.

        int doubles = 0;

        while (true) {

          long pos = file.getFilePointer();

          byte[] check = new byte[5];
          file.readFully(check);

          if (check[0] == 0 && check[2] == 0 && check[3] == 0 && check[4] == 0 && (check[1] > 0x00 && check[1] < 0x04)) {

            for (int i = 0; i < check[1]; ++i) {
              Double.longBitsToDouble(Long.reverseBytes(file.readLong()));
              doubles++;
            }

            break;

          } else {

            file.seek(pos);
            Double.longBitsToDouble(Long.reverseBytes(file.readLong()));
            doubles++;

          }

        }

        sb.has3D(doubles == 3);

        b.type(sb.build());
        break;

      }
      case 9: {
        // raster
        throw new IllegalArgumentException(String.format("Unsupported field type '%s' (raster)", geotype));
        // break;
      }

      default:
        throw new IllegalArgumentException(String.format("Unknown field type '%s'", geotype));
    }

    return b.build();

  }

  private int readUnsignedVarInt() throws IOException {
    return file.readVarUInt32();
  }

  private String readUTFString() throws IOException {

    int utf16len = file.readUnsignedByte();

    if (utf16len == 0) {
      return null;
    }

    if (utf16len < 0 || utf16len > 256) {
      throw new IllegalArgumentException();
    }

    int remain = utf16len;
    char[] data = new char[utf16len];

    while (remain > 0) {
      char c = Character.reverseBytes(file.readChar());
      data[utf16len - remain] = c;
      remain--;
    }

    // byte[] name = new byte[utf16len];
    //
    // file.readFully(name);

    return new String(data);
  }

  public void close() {
    this.file.close();
  }

  /**
   * fetches a field and the associated rows.
   */

  GeoFeature getRow(long featureId, long offset) {

    try {

      GeoFeature.GeoFeatureBuilder fb = GeoFeature.builder();

      fb.featureId(featureId);

      fb.fields(this.fields);

      try {

        file.seek(offset);

        // the length of the data ..
        int blobLen = file.readInt32();

        if (blobLen < 0) {
          throw new GeoDBException(String.format("Crazy sized row at byte offset %d (%d)", offset, blobLen));
        }

        int pos = (int) file.getFilePointer();
        byte[] buf = new byte[blobLen];
        file.readFully(buf);
        file.seek(pos);

        int nullable = (int) Math.ceil(getNullableFields().size() / 8.0);

        byte[] flags = new byte[nullable];

        file.readFully(flags);

        int nullflagpos = 0;

        int id = 0;

        BitSet nullflags = BitSet.valueOf(flags);

        for (Field f : this.fields) {

          boolean nulled = (f.getType().isNullable()) && nullflags.get(nullflagpos);

          if (f.getType().isNullable()) {
            ++nullflagpos;
          }

          if (this.fidId == null || this.fidId != id) {

            if (nulled) {
              fb.value(f.getType().getDefaultValue());
            } else {
              // read the field.
              fb.value(f.getType().read(file));
            }

          }

          ++id;

        }

        return fb.build();

      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    } catch (Exception ex) {
      throw new GeoDBException(String.format("While processing FID %d", featureId), ex);
    }

  }

  private List<Field> getNullableFields() {
    return this.fields.stream().filter(f -> f.getType().isNullable()).collect(Collectors.toList());
  }

  public long getRowCount() {
    return this.header.getRows();
  }

  public List<Field> getFields() {
    return this.fields;
  }

  public int getVersion() {
    return this.header.getVersion();
  }

  public int getField(String name) {
    name = name.toLowerCase().trim();
    for (int i = 0; i < this.fields.size(); ++i) {
      String fname = fields.get(i).getName().trim().toLowerCase();
      if (fname.equals(name)) {
        return i;
      }
    }
    throw new IllegalArgumentException(String.format("No such field: '%s'", name));
  }

}
