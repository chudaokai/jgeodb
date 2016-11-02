package io.zrz.jgdb;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Factory for opening GDB files.
 * 
 * we open an initial file to find the verison
 * 
 * @author Theo Zourzouvillys
 *
 */

public class FileGDBFactory {

  /**
   * Fetches the version of the GDB.
   * 
   * @param dir
   *          the database.
   * 
   * @return The version number, or absent if this isn't a GDB folder.
   */

  public static Optional<Integer> getVersionFromDirectory(Path dir) {
    try (RandomAccessFile raf = new RandomAccessFile(dir.resolve("gdb").toFile(), "r")) {
      byte version = raf.readByte();
      return Optional.of((int) version);
    } catch (IOException ex) {
      throw new GeoDBException(ex);
    }
  }

  public static GeoDB open(final Path dir) {

    Optional<Integer> gdbver = getVersionFromDirectory(dir);

    if (!gdbver.isPresent()) {
      throw new IllegalArgumentException(String.format("%s is not a file GDB folder"));
    }

    switch (gdbver.get()) {
      case 2:
        // version 9
        return GeoDB_R4.open(dir);
      case 5:
        // version 10.
        return GeoDB_R5.open(dir);
      default:
        throw new GeoDBException(String.format("Unknown GeoDB version '%d'", gdbver.get()));
    }
    

  }

}
