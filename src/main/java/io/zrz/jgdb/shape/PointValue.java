package io.zrz.jgdb.shape;

import lombok.Value;

@Value
public class PointValue implements GeometryValue {

  double x, y, z, m;

  @Override
  public void visit(GeometryValueVisitor visitor) {
    visitor.visitPointValue(this);
  }

}
