#!/bin/bash

schema_id="1"
messages='{ "type": "DATASET_REASONED", "fdkId": "6467f35c-f246-48f5-956c-616b949e52e6", "timestamp": 1647698566000, "graph": "ADD VALID TURTLE HERE" }
          { "type": "DATASET_REASONED", "fdkId": "28df0d6b-8e75-4136-84f3-f3f3c65fbd7c", "timestamp": 1647698566000, "graph": "ADD VALID TURTLE HERE" }
          { "type": "DATASET_REMOVED", "fdkId": "2680f16a-22ed-4239-ae9d-259791403fee", "timestamp": 1647698566000, "graph": "" }'

docker-compose exec schema-registry bash -c "echo '$messages'|kafka-avro-console-producer --bootstrap-server kafka:29092 --topic dataset-events --property value.schema.id=${schema_id}"