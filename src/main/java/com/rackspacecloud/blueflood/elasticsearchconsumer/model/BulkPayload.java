package com.rackspacecloud.blueflood.elasticsearchconsumer.model;

import lombok.Data;

@Data
public class BulkPayload {
    String indexingMetadata;
    String metricInformation;

    @Override
    public String toString(){
        return (String.format("%s\n%s\n", indexingMetadata, metricInformation));
    }
}
