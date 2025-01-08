package io.zrz.jgdb.shape;

public  class MultiPoint implements GeometryValue {
  public Point points[];

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("MultiPoint(");
    if(points==null || points.length==0){
      sb.deleteCharAt(sb.length()-1);
      sb.append(" EMPTY");
    }else {
      for (int i = 0; i < points.length; ++i) {
        if (i > 0)
          sb.append("\n, ");
        sb.append("(").append(points[i].toString()).append(")");
      }
      sb.append(")");
    }
    return sb.toString();
  }

  @Override
  public void visit(GeometryValueVisitor visitor) {
    visitor.visitMultipoint(this);
  }

}