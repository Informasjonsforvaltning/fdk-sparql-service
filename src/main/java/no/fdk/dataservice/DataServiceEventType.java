/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package no.fdk.dataservice;
@org.apache.avro.specific.AvroGenerated
public enum DataServiceEventType implements org.apache.avro.generic.GenericEnumSymbol<DataServiceEventType> {
  DATA_SERVICE_REASONED, DATA_SERVICE_REMOVED, DATA_SERVICE_HARVESTED  ;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"DataServiceEventType\",\"namespace\":\"no.fdk.dataservice\",\"symbols\":[\"DATA_SERVICE_REASONED\",\"DATA_SERVICE_REMOVED\",\"DATA_SERVICE_HARVESTED\"]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  @Override
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
}
