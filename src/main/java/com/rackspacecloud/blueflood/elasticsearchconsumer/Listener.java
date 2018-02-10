package com.rackspacecloud.blueflood.elasticsearchconsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rackspacecloud.blueflood.elasticsearchconsumer.model.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@Component
public class Listener {
    @Autowired
    RestTemplate restTemplate;

    @Value("${elasticsearch.url}")
    private String elasticsearchIndexUrl;

    @Value("${elasticsearch.index.name}")
    private String elasticsearchIndexName;

    @Value("${elasticsearch.type}")
    private String elasticsearchType;

    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);

    private CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = "#{'${kafka.topics.in}'.split(',')}")
    public void listenBluefloodMetrics(ConsumerRecord<?, ?> record) {
        //TODO: Get tracking ID from the record to stitch the tracing
        String passedTrackingId = "";
        String currentTrackingId = "";

        currentTrackingId = StringUtils.isEmpty(passedTrackingId)
                ? UUID.randomUUID().toString() : String.format("%s|%s", passedTrackingId, currentTrackingId);

        LOGGER.info("TrackingId:{}, START: Processing", currentTrackingId);
        LOGGER.debug("TrackingId:{}, Received payload:{}", currentTrackingId, record);
        String strRecord = record.value().toString();

        ObjectMapper objectMapper = new ObjectMapper();
        Input input = null;
        try {
            input = objectMapper.readValue(strRecord, Input.class);
        } catch (IOException e) {
            LOGGER.error("TrackingId:{}, Exception message: {}, Stack trace: {}",
                    currentTrackingId, e.getMessage(), e.getStackTrace());
        }

        String tenantId = input.getTenantId();
        String metricName = input.getMetricString();

        IndexingMetadata indexingMetadata = getIndexingMetadata(tenantId, metricName);

        MetricInformation metricInformation = new MetricInformation();
        metricInformation.setTenantId(tenantId);
        metricInformation.setMetricName(metricName);

        BulkPayload bulkPayload = new BulkPayload();

        try {
            bulkPayload.setIndexingMetadata(objectMapper.writeValueAsString(indexingMetadata));
            bulkPayload.setMetricInformation(objectMapper.writeValueAsString(metricInformation));
        } catch (JsonProcessingException e) {
            LOGGER.error("TrackingId:{}, Exception message: {}, Stack trace: {}",
                    currentTrackingId, e.getMessage(), e.getStackTrace());
        }

        indexIntoElasticsearch(currentTrackingId, strRecord, bulkPayload);
        LOGGER.info("TrackingId:{}, FINISH: Processing", currentTrackingId);
    }

    private IndexingMetadata getIndexingMetadata(String tenantId, String metricName) {
        Index index = new Index();
        index.setIndex(elasticsearchIndexName);
        index.setType(elasticsearchType);
        index.setId(String.format("%s:%s", tenantId, metricName));
        index.setRouting(tenantId);

        IndexingMetadata indexingMetadata = new IndexingMetadata();
        indexingMetadata.setIndex(index);
        return indexingMetadata;
    }

    private void indexIntoElasticsearch(String currentTrackingId, String strRecord, BulkPayload bulkPayload) {
        String payload = bulkPayload.toString();

        HttpEntity<String> request = new HttpEntity<>(payload);
        String url = String.format("%s/_bulk", elasticsearchIndexUrl);
        ResponseEntity<BulkPayload> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, request, BulkPayload.class);
        }
        catch(Exception ex){
            LOGGER.error("TrackingId:{}, Using url [{}], Exception message: {}",
                    currentTrackingId, url, ex.getMessage());
        }

        if(response.getStatusCode() != HttpStatus.OK){
            LOGGER.error("TrackingId:{}, Using elasticsearch url [{}], couldn't index the payload -> {}",
                    currentTrackingId, url, payload);
        }
        else{
            LOGGER.info("TrackingId:{}, Successfully indexed payload in elasticsearch -> {}",
                    currentTrackingId, strRecord);
        }
    }
}
