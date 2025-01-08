package io.zrz.jgdb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import io.zrz.jgdb.GeoIndexFile.IndexEntry;

public class GeoTable implements AutoCloseable, GeoLayer {

  private final AbstractGeoDB db;
  private final long id;
  private FileGDBTable table;
  private GeoIndexFile index;

  GeoTable(final AbstractGeoDB db, final long id) {
    this.db = db;
    this.id = id;
  }

  public int getTableId() {
    return (int) this.id;
  }

  public int getVersion() {
    return this.table.getVersion();
  }

  public boolean open() {

    final Path file = this.db.getTableFile(this.id, "gdbtable");

    if (!Files.exists(file)) {
      return false;
    }

    // open the table itself.
    try {
      this.table = new FileGDBTable(file);
    } catch (final IOException e) {
      this.close();
      throw new GeoDBException(e);
    }

    // open the index.
    try {
      this.index = new GeoIndexFile(this.db.getTableFile(this.id, "gdbtablx"));
    } catch (final IOException e) {
      this.close();
      throw new GeoDBException(e);
    }

    return true;

  }

  /**
   * Fetches the given feature ID from this table. The feature ID is 1 based,
   * not zero based.
   * 
   * @param featureId
   * @return
   */

  @Override
  public GeoFeature getFeature(final int featureId) {
    final int offset = this.index.getFeatureOffset(featureId - 1);
    if (offset == -1) {
      return null;
    }
    return this.table.getRow(featureId, offset);
  }

  @Override
  public void close() {
    if (this.table != null) {
      this.table.close();
      this.table = null;
    }
    if (this.index != null) {
      this.index.close();
      this.index = null;
    }
    this.db.closed(this.id);

  }

  @Override
  public int getFeatureCount() {
    return (int) this.table.getRowCount();
  }

  public List<GeoField> getFields() {
    return this.table.getFields();
  }

  public GeoField getField(int id) {
    return table.getFields().get(id);
  }

  public GeoField getField(String name) {
    return table.getFields().get(getFieldId(name));
  }

  public int getFieldId(String name) {
    return this.table.getField(name);
  }

  /**
   * 
   */

  public Optional<GeoField> getFeatureIdField() {
    return this.table.getFields().stream().filter(a -> "objid".equalsIgnoreCase(a.getName())).findAny();
  }

  @Override
  public boolean hasGeometry() {
    return this.table.getFields().stream().anyMatch(a -> "shape".equalsIgnoreCase(a.getName()));
  }

  /**
   * 
   */

  public GeoField getShapeField() {
    return this.table.getFields().stream().filter(a ->"shape".equalsIgnoreCase(a.getName())).findAny().orElse(null);
  }

  /**
   * 
   * @param db
   * @param idx
   * @return
   */

  static GeoTable open(final AbstractGeoDB db, final long idx) {
    final GeoTable table = new GeoTable(db, idx);
    if (!table.open()) {
      return null;
    }
    return table;
  }
  
  /**
   * 
   */

  @Override
  public Iterator<GeoFeature> iterator() {

    return new Iterator<GeoFeature>() {

      private final Iterator<IndexEntry> it = index.iterator();

      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public GeoFeature next() {
        
        final IndexEntry next = it.next();
        
        if (next == null) {
          return null;
        }
        
        return table.getRow(next.getObjectId(), next.getOffset());
      }

    };

  }

}
