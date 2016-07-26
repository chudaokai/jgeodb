package io.zrz.jgdb;

import java.util.List;

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
      if (fields.get(i).getName().equals(name)) {
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

      if (i > 0) {
        sb.append(", ");
      }
      
      sb.append(fields.get(i).getName()).append("=").append(values.get(i));
      

    }

    return sb.toString();

  }

}
