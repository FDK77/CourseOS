
# Индекс для gateway
curl -X POST "http://localhost:5601/api/saved_objects/index-pattern/gateway-logs-*" \
     -H "kbn-xsrf: true" \
     -H "Content-Type: application/json" \
     -d '{
           "attributes": {
             "title": "gateway-logs-*",
             "timeFieldName": "@timestamp"
           }
         }'

# Индекс для domain-service
curl -X POST "http://localhost:5601/api/saved_objects/index-pattern/domain-logs-*" \
     -H "kbn-xsrf: true" \
     -H "Content-Type: application/json" \
     -d '{
           "attributes": {
             "title": "domain-logs-*",
             "timeFieldName": "@timestamp"
           }
         }'