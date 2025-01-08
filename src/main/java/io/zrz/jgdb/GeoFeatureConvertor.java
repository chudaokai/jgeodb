package io.zrz.jgdb;

@FunctionalInterface
public interface GeoFeatureConvertor<R, T> {
    R convert(T ent);
}
