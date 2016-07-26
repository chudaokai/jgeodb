package io.zrz.jgdb.shape;

public interface GeometryValueVisitor {

  void visitPointValue(PointValue pointValue);

  void visitMultipoint(MultiPoint multiPoint);

}
