package io.zrz.jgdb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Common functionality between V9 and V10 databases.
 * 
 * @author Theo Zourzouvillys
 *
 */

abstract class AbstractGeoDB implements GeoDB {

  /**
   * The base path for the database.
   */

  protected final Path dir;

  /**
   * All of the files in the database.
   */

  protected List<Path> files;

  /**
   * tables that are currently open.
   */

  private Set<GeoFeatureTable> opened = new HashSet<>();

  /**
   * 
   */

  AbstractGeoDB(final Path dir) {
    this.dir = dir;
  }

  /**
   * Check if the specified table exists.
   * 
   * @param tableId
   *          The identifier for the table.
   * @return True if the given table identifier exists.
   */

  protected boolean tableExists(long tableId) {
    return Files.exists(getTableFile(tableId, "gdbtable"));
  }

  /**
   * fetches the path to the specified table/extension.
   * 
   * @param id
   * @param ext
   * @return
   */

  Path getTableFile(final long id, final String ext) {
    return this.dir.resolve(makeFileName(id, ext));
  }

  /**
   * returns the filename for the given table identifier.
   * 
   * @param id
   *          The identifier of the table to open
   * @param ext
   *          The extension, e.g gdbtable, gdbtablx, spx, freelist, etc.
   * @return The name of the file.
   */

  static String makeFileName(final long id, final String ext) {
    return String.format("a%08x.%s", id, ext);
  }

  /**
   * performs open behaviour common between versions of the database.
   */

  final void open() {

    try {
      this.files = Files.list(this.dir).collect(Collectors.toList());
    } catch (final IOException e) {
      throw new GeoDBException(e);
    }

    // open the catalog, which is version specific.
    this.openCatalog();

  }

  @Override
  public final void close() {
    // copy open set, then close them all.
    new HashSet<>(this.opened).forEach(table -> table.close());
  }

  /**
   * Opens the catalog for this version. Called after processing the abstract
   * "open", and should populate everything needed to list available layers, etc
   * as well as verify that everything needed for high level operation is
   * available.
   */

  abstract protected void openCatalog();

  /**
   * Opens a gdbtable/gdbtablx pair.
   * 
   * @param tableId
   * @param tableVersion
   * @return
   */

  protected GeoFeatureTable openTable(long tableId, int tableVersion) {
    final GeoFeatureTable index = GeoFeatureTable.open(this, tableId);
    if (index.getVersion() != tableVersion) {
      throw new IllegalArgumentException(String.format("Unexpected Table Version: %d", index.getVersion()));
    }
    this.opened.add(index);
    return index;
  }

  public void closed(GeoFeatureTable table) {
    this.opened.remove(table);
  }

}
