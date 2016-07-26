package io.zrz.jgdb;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Value;

public class FileGDBTable {

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
  }

  private final RandomAccessFile xfile;
  private Header header;
  private List<GeoField> fields;

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

  public FileGDBTable(final Path path) throws IOException {
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

  public List<GeoField> readFields() throws IOException {

    int fields = file.readInt16();

    if (fields < 0 || fields > 65535) {
      throw new IllegalArgumentException(String.format("Invalid fields count: %d", fields));
    }

    //
    List<GeoField> ret = new ArrayList<>();

    for (int i = 0; i < fields; ++i) {

      GeoField field = readField();

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

  private GeoField readField() throws IOException {

    GeoField.GeoFieldBuilder b = GeoField.builder();

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

        for (GeoField f : this.fields) {

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

  private List<GeoField> getNullableFields() {
    return this.fields.stream().filter(f -> f.getType().isNullable()).collect(Collectors.toList());
  }

  public long getRowCount() {
    return this.header.getRows();
  }

  public List<GeoField> getFields() {
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
