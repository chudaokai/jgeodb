package io.zrz.jgdb.shape;

public class LineString extends MultiPoint {
    @Override
    public String toString() {
        String str = super.toString();
        return str.replace("MultiPoint","LineString");
    }

}
