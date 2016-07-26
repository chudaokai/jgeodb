package io.zrz.jgdb.shape;

public class Point {

  public double x[];
  public double y[];
  public double z[];
  public double m[];

  public String toString() {

    StringBuilder sb = new StringBuilder();

    sb.append("(");

    for (int i = 0; i < x.length; ++i) {
      if (i > 0)
        sb.append(", ");
      sb.append(x[i]).append(" ").append(y[i]);
    }

    sb.append(")");

    return sb.toString();

  }

}
