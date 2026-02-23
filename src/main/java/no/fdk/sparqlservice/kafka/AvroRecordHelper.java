package no.fdk.sparqlservice.kafka;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecord;

/**
 * Extracts fields from Kafka event payloads that may be either GenericRecord or SpecificRecord.
 * Optional fields harvestRunId and uri are read with runCatch (safe extraction, null on missing/error).
 */
public final class AvroRecordHelper {

    private AvroRecordHelper() {
    }

    /**
     * Safe extraction for optional string fields (e.g. harvestRunId, uri). Returns null if missing or on any error.
     */
    public static String getOptionalString(Object record, String fieldName) {
        if (record == null) {
            return null;
        }
        try {
            if (record instanceof GenericRecord) {
                Object value = ((GenericRecord) record).get(fieldName);
                return value == null ? null : value.toString();
            }
            if (record instanceof SpecificRecord) {
                SpecificRecord specific = (SpecificRecord) record;
                Object value = specific.get(specific.getSchema().getField(fieldName).pos());
                return value == null ? null : value.toString();
            }
        } catch (Exception ignored) {
            // runCatch: optional field, return null on any error
        }
        return null;
    }

    public static String getRequiredString(Object record, String fieldName) {
        if (record == null) {
            return null;
        }
        try {
            if (record instanceof GenericRecord) {
                Object value = ((GenericRecord) record).get(fieldName);
                return value == null ? null : value.toString();
            }
            if (record instanceof SpecificRecord) {
                SpecificRecord specific = (SpecificRecord) record;
                Object value = specific.get(specific.getSchema().getField(fieldName).pos());
                return value == null ? null : value.toString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Long getRequiredLong(Object record, String fieldName) {
        if (record == null) {
            return null;
        }
        try {
            if (record instanceof GenericRecord) {
                Object value = ((GenericRecord) record).get(fieldName);
                return value == null ? null : ((Number) value).longValue();
            }
            if (record instanceof SpecificRecord) {
                SpecificRecord specific = (SpecificRecord) record;
                Object value = specific.get(specific.getSchema().getField(fieldName).pos());
                return value == null ? null : ((Number) value).longValue();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** Type field is an enum; compare by symbol name (e.g. "SERVICE_REASONED"). */
    public static boolean isType(Object record, String typeSymbol) {
        if (record == null || typeSymbol == null) {
            return false;
        }
        try {
            String typeField = "type";
            Object typeObj;
            if (record instanceof GenericRecord) {
                typeObj = ((GenericRecord) record).get(typeField);
            } else if (record instanceof SpecificRecord) {
                SpecificRecord specific = (SpecificRecord) record;
                typeObj = specific.get(specific.getSchema().getField(typeField).pos());
            } else {
                return false;
            }
            return typeObj != null && typeObj.toString().equals(typeSymbol);
        } catch (Exception ignored) {
            return false;
        }
    }
}
