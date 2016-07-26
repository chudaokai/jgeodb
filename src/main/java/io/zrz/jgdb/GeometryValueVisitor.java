package io.zrz.jgdb;

import io.zrz.jgdb.GeoTableFile.MultiPoint;
import io.zrz.jgdb.GeometryValue.PointValue;

public interface GeometryValueVisitor {

  void visitPointValue(PointValue pointValue);

  void visitMultipoint(MultiPoint multiPoint);

}
