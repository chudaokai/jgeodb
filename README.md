# Java FileGDB

A dependency-free basic read-only native Java implementation of FileGDB, based on the [C++ OpenFileGDB in GDAL](http://www.gdal.org/drv_openfilegdb.html) and [this](https://github.com/rouault/dump_gdbtable/wiki/FGDB-Spec) awesome reverse engineering work. 

Supports:

  - Scanning layers
  - Fetching a specific feature
  - All data types
  - Version 9 and 10 formats
 
Doesn't Support (pull requests accepted!):
 
  - Using Indexes
  - Write Support

## Usage

``` java
  try (GeoDB db = FileGDBFactory.open(Paths.get("path/to/gdb/folder"))) {
    GeoLayer layer = db.openLayer("My_Layer");
    
    // fetch a specific feature from this layer.
    GeoFeature feature = layer.getFeature(1234);
    
    // or ...
    
    // iterate through each feature in the layer.
    layer.forEach(feature -> dumpFeature(feature));
    
  }
  
  void dumpFeature(GeoFeature feature) {
  
    for (Field field : feature.getFields()) {
    
    }
  
  }
  
  
```

## Reporting Bugs

If you find a bug, please use GitHub issue tracking, and if possible make sure you include a link to a ZIP/tarball of a File GDB that replicates the issue.

## Author

Theo Zourzouvillys <theo@zrz.io>.
