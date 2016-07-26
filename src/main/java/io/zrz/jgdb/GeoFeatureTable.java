package io.zrz.jgdb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import io.zrz.jgdb.GeoTableFile.Field;

public class GeoFeatureTable implements AutoCloseable {

  private final AbstractGeoDB db;
  private final long id;
  private GeoTableFile table;
  private GeoIndexFile index;

  GeoFeatureTable(final AbstractGeoDB db, final long id) {
    this.db = db;
    this.id = id;
  }

  public long getTableId() {
    return this.id;
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
      this.table = new GeoTableFile(file);
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

  public Object getFeature(final int featureId) {
    try {
      final int offset = this.index.getFeatureOffset(featureId - 1);
      if (offset == 0) {
        return null;
      }
      return this.table.getRow(featureId, offset);
    } catch (final IOException e) {
      this.close();
      throw new GeoDBException(e);
    }
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
    this.db.closed(this);
   
  }

  public void scan(final RowConsumer feature) {
    this.index.scan(rowid -> true, (objid, offset) -> feature.accept(GeoFeatureTable.this.table.getRow(objid, offset)));
  }

  public void scan(final Predicate<Long> acceptor, final RowConsumer feature) {
    this.index.scan(acceptor, (objid, offset) -> feature.accept(GeoFeatureTable.this.table.getRow(objid, offset)));
  }

  public long getFeatureCount() {
    return this.table.getRowCount();
  }

  public List<Field> getFields() {
    return this.table.getFields();
  }

  public Field getField(int id) {
    return table.getFields().get(id);
  }

  public Field getField(String name) {
    return table.getFields().get(getFieldId(name));
  }

  public int getFieldId(String name) {
    return this.table.getField(name);
  }

  /**
   * 
   */

  public Optional<Field> getFeatureIdField() {
    return this.table.getFields().stream().filter(a -> a.getName().toLowerCase().equals("objid")).findAny();
  }

  /**
   * 
   */

  public Optional<Field> getShapeField() {
    return this.table.getFields().stream().filter(a -> a.getName().toLowerCase().equals("shape")).findAny();
  }

  /**
   * 
   * @param db
   * @param idx
   * @return
   */

  static GeoFeatureTable open(final AbstractGeoDB db, final long idx) {
    final GeoFeatureTable table = new GeoFeatureTable(db, idx);
    if (!table.open()) {
      return null;
    }
    return table;
  }

}
