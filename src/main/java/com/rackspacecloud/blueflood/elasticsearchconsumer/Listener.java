package com.rackspacecloud.blueflood.elasticsearchconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rackspacecloud.blueflood.elasticsearchconsumer.model.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@Component
public class Listener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Listener.class);

    private CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(id = "blueflood-metrics-listener", topicPartitions =
            { @TopicPartition(topic = "blueflood-metrics",
                    partitionOffsets = @PartitionOffset(partition = "0", initialOffset = "0"))
            })
    public void listenBluefloodMetrics(ConsumerRecord<?, ?> record) throws IOException {
        LOGGER.info("Received payload='{}'", record);
        String strRecord = record.value().toString();

        ObjectMapper objectMapper = new ObjectMapper();
        Input input = objectMapper.readValue(strRecord, Input.class);

        String tenantId = input.getTenantId();
        String metricName = input.getMetricString();

        Index index = new Index();
        index.setIndex("metric_metadata");
        index.setType("metrics");
        index.setId(String.format("%s:%s", tenantId, metricName));
        index.setRouting(tenantId);

        IndexingMetadata indexingMetadata = new IndexingMetadata();
        indexingMetadata.setIndex(index);

        MetricInformation metricInformation = new MetricInformation();
        metricInformation.setTenantId(tenantId);
        metricInformation.setMetricName(metricName);

        BulkPayload bulkPayload = new BulkPayload();
        bulkPayload.setIndexingMetadata(objectMapper.writeValueAsString(indexingMetadata));
        bulkPayload.setMetricInformation(objectMapper.writeValueAsString(metricInformation));

        String payload = bulkPayload.toString();

        String url = "http://127.0.0.1:9200/_bulk";

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> request = new HttpEntity<>(payload);

        ResponseEntity<BulkPayload> response = restTemplate.exchange(url, HttpMethod.POST, request, BulkPayload.class);
        if(response.getStatusCode() != HttpStatus.OK){
            LOGGER.error("Couldn't index the payload -> {}", payload);
        }
        else{
            System.out.println("Successfully indexed payload in ES -> " + strRecord);
        }
    }
}