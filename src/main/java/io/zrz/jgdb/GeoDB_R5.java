package io.zrz.jgdb;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import io.zrz.jgdb.shape.GeometryValue;
import lombok.Builder;
import lombok.Value;

/**
 * @see <a href='https://github.com/rouault/dump_gdbtable/wiki/FGDB-Spec'>this</a>
 */

final class GeoDB_R5 extends AbstractGeoDB {

    // private static final int MIN_FEATURE_TABLE_ID = 0x3E;

    private static final int GDB_SystemCatalog_ID = 0x1;
    // private static final int GDB_DBTune_ID = 0x2;
    // private static final int GDB_SpatialRefs_ID = 0x3;
    // private static final int GDB_Release_ID = 0x4;
    // private static final int GDB_FeatureDataset_ID = 0x5;
    // private static final int GDB_ObjectClasses_ID = 0x6;
    // private static final int GDB_FeatureClasses_ID = 0x7;
    // private static final int GDB_FieldInfo_ID = 0x8;
    //
    // private static final int MIN_SYSTEM_TABLE = 36;

    private static final int TABLE_VERSION = 4;

    protected final Map<String, Long> catalog = new HashMap<>();

    private V10_Items items;

    GeoDB_R5(Path dir) {
        super(dir);
    }

    @Override
    protected void openCatalog() {

        // first, build the names and mappings.

        GeoTable index = this.openTable(GDB_SystemCatalog_ID, TABLE_VERSION);

        // fetch all of the non system tables.
        index.forEach((feature) -> {
            this.catalog.put(feature.getValue(0).stringValue(), feature.getFeatureId());
        });

        index.close();

        //
        // only if V10:
        this.items = new V10_Items(this);

    }

    @Override
    public GeoTable layer(String name) {
        if(this.items!=null && this.items.getItemByName(name)==null){
            throw new GeoDBException(name+" not a Feature Class.");
        }
        Long fid = this.catalog.get(name);
        if (fid == null) {
            throw new IllegalArgumentException(name);
        }
        return super.openTable(fid);
    }

    public GeoTable getFeatureTableByLayerId(final int layer) {
        return null;
    }

    public List<String> getLayers() {
        return getLayers(null);
    }

    @Override
    public List<String> getLayers(String datasetName) {
        if (datasetName != null && !datasetName.isEmpty()) {
            datasetName += "\\";
        } else {
            datasetName = "";
        }
        String prefix = "\\" + datasetName;
        return this.items.items.entrySet().stream().filter(m -> m.getValue().path != null && catalog.containsKey(m.getKey()) &&
                m.getValue().path.startsWith(prefix)).map(Map.Entry::getKey).sorted(String::compareTo).collect(Collectors.toList());
    }

    @Override
    public Map<String, List<String>> getLayerTree() {
        return items.items.entrySet().stream().filter(m->catalog.containsKey(m.getKey())).collect(Collectors.toMap(
                m->{
                    String path = m.getValue().getPath();
                    if(("\\"+m.getKey()).equals(path)){
                        return "";
                    }else{
                        return path.substring(1,path.indexOf('\\',1));
                    }
                },m->new ArrayList<String>(){{add(m.getKey());}},(m,n)->{m.addAll(n);return m;}
        ));
    }

    @Override
    public List<String> getDatasets() {
        return this.items.items.entrySet().stream().filter(m -> !"workspace".equalsIgnoreCase(m.getValue().physicalName)
                && !catalog.containsKey(m.getKey())).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public static GeoDB_R5 open(Path dir) {
        final GeoDB_R5 db = new GeoDB_R5(dir);
        db.open();
        return db;
    }

    @Override
    public FileGDBVersion getVersion() {
        return FileGDBVersion.V10;
    }

    @Value
    @Builder
    static class Item {
        private UUID uuid;
        private String type;
        private String physicalName;
        private String path;
        private int datasetSubtype1;
        private int datasetSubtype2;
        private String datasetInfo1;
        private String datasetInfo2;
        private int properties;
        private GeometryValue shape;
    }

    class V10_Items {

        private Map<String, Item> items = new HashMap<>();

        public V10_Items(AbstractGeoDB db) {

            try (GeoTable table = layer("GDB_Items")) {

                table.forEach((RowConsumer) feature -> {
                    if (!feature.getValue("Name").isNulled()) {
                        Item.ItemBuilder ib = Item.builder();
                        ib.uuid(feature.getValue("UUID").uuidValue());
                        ib.type(feature.getValue("Type").stringValue());
                        ib.physicalName(feature.getValue("PhysicalName").stringValue());
                        if (!feature.getValue("Path").isNulled())
                            ib.path(feature.getValue("Path").stringValue());

                        ib.datasetSubtype1(feature.getValue("DatasetSubtype1").intValue());
                        ib.datasetSubtype2(feature.getValue("DatasetSubtype2").intValue());
                        ib.datasetInfo1(feature.getValue("DatasetInfo1").stringValue());
                        ib.datasetInfo2(feature.getValue("DatasetInfo2").stringValue());

                        ib.properties(feature.getValue("Properties").intValue());

                        if (!feature.getValue("Shape").isNulled()) {
                            ib.shape(feature.getValue("Shape").geometryValue());
                        }

                        items.put(feature.getValue("Name").stringValue(), ib.build());

                    }

                });

            }
        }

        public Item getItemByName(String name) {
            return items.get(name);
        }

    }
}
