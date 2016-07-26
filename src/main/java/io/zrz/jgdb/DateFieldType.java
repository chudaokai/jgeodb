package io.zrz.jgdb;

import java.io.IOException;
import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class DateFieldType implements FieldType {

  private static Instant EPOCH = Instant.parse("1899-12-30T00:00:00.00Z");

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