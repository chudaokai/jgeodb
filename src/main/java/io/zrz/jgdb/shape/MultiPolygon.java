package io.zrz.jgdb.shape;

public class MultiPolygon extends MultiPoint{
    @Override
    public String toString() {
        String str = super.toString();
        return str.replace("MultiPoint","MultiPolygon");
    }
}
