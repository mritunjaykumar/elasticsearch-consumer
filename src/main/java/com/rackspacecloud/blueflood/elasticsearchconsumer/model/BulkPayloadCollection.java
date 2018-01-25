package com.rackspacecloud.blueflood.elasticsearchconsumer.model;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BulkPayloadCollection {
    List<BulkPayload> bulkPayloads;

    public BulkPayloadCollection(){
        bulkPayloads = new ArrayList<>();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        for(BulkPayload payload : bulkPayloads){
            sb.append(payload.toString());
        }

        return sb.toString();
    }
}
