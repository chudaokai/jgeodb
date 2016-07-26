package io.zrz.jgdb;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GeoField {
  private final String name;
  private final String alias;
  private final FieldType type;
}
