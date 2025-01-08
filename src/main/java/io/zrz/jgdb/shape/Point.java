package io.zrz.jgdb.shape;

public class Point {

  public double x[];
  public double y[];
  public double z[];
  public double m[];

  public String toString() {
    if(x==null ||x.length==0){
      return "EMPTY";
    }
    String format = x[0]<=180?"%.12f":"%.8f";
    StringBuilder sb = new StringBuilder();
    sb.append("(");

    for (int i = 0; i < x.length; ++i) {
      if (i > 0)
        sb.append(", ");
      sb.append(String.format(format,x[i])).append(" ").append(String.format(format,y[i]));
    }

    sb.append(")");
    return sb.toString();
  }
}
