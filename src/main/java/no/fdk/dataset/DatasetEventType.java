/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package no.fdk.dataset;
@org.apache.avro.specific.AvroGenerated
public enum DatasetEventType implements org.apache.avro.generic.GenericEnumSymbol<DatasetEventType> {
  DATASET_REASONED, DATASET_REMOVED, DATASET_HARVESTED  ;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"DatasetEventType\",\"namespace\":\"no.fdk.dataset\",\"symbols\":[\"DATASET_REASONED\",\"DATASET_REMOVED\",\"DATASET_HARVESTED\"]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  @Override
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
}