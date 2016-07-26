package io.zrz.jgdb;

public class GeoDBException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public GeoDBException(final Exception ex) {
    super(ex);
  }

  public GeoDBException(String string) {
    super(string);
  }

  public GeoDBException(String message, Throwable ex) {
    super(message, ex);
  }

}
