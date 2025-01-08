package io.zrz.jgdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Singular;

/**
 * 
 * @author Theo Zourzouvillys
 *
 */

@Builder
public class GeoFeature {

  private long featureId;

  private List<GeoField> fields;

  @Singular
  private List<Object> values;

  public GeoFieldValue getValue(int fieldId) {
    return new GeoFieldValue(fields.get(fieldId), values.get(fieldId));
  }

  public GeoFieldValue getValue(String name) {
    return getValue(getFieldId(name));
  }

  public int getFieldId(String name) {
    for (int i = 0; i < fields.size(); ++i) {
      if (fields.get(i).getName().equalsIgnoreCase(name)) {
        return i;
      }
    }
    throw new IllegalArgumentException(name);
  }

  public long getFeatureId() {
    return featureId;
  }

  public List<GeoField> getFields() {
    return this.fields;
  }

  /**
   * 
   */

  public String toString() {

    StringBuilder sb = new StringBuilder();

    sb.append("[").append(this.featureId).append("] ");

    for (int i = 0; i < fields.size(); ++i) {

      if (values.get(i) == null) {
        continue;
      }

      if (i > 0) {
        sb.append(", ");
      }

      String desc = values.get(i).toString();

      sb.append(fields.get(i).getName()).append("=").append(desc.substring(0, Math.min(30, desc.length())));

    }

    return sb.toString();

  }

  public <R> Map<String, R> toMap(GeoValueVisitor<R> converter) {

    Map<String, R> ret = new HashMap<>();

    for (int i = 0; i < fields.size(); ++i) {
      GeoField f = fields.get(i);
      Object value = values.get(i);
      
      if (value != null) {
        
        R v = f.getType().apply(value, converter);
        
        if (v != null) {
          ret.put(f.getName(), v);
        }
        
      }
    }

    return ret;

  }

}
