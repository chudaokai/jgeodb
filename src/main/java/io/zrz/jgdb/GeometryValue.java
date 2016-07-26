package io.zrz.jgdb;

import lombok.Value;

/**
 * a stored geometry value.
 * 
 * note that we implement some simple value objects rather than pull in JTS (or
 * similar), to avoid the additional dependency, as this interface and it's
 * implementations are exposed.
 * 
 * @author Theo Zourzouvillys
 *
 */

public interface GeometryValue {

  void visit(GeometryValueVisitor visitor);

  @Value
  public static class PointValue implements GeometryValue {

    double x, y, z, m;

    @Override
    public void visit(GeometryValueVisitor visitor) {
      visitor.visitPointValue(this);
    }

  }

}
