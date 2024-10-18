/*
 * Copyright (c) 2012-2024 Savoir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.savoir.soa.rag.ref.arch.etl;

import dev.langchain4j.data.document.Metadata;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import dev.langchain4j.data.segment.TextSegment;

public class TransformProcessor implements Processor {

    private Random rnd = new Random();
    private List<String> loyaltyLevels = List.of("bronze", "silver", "gold");

    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        //Use body and data sources to generate metadata for this embedding.
        Metadata metadata = new Metadata();
        metadata.put("tenant", "savoir");
        metadata.put("chargeBacks", String.valueOf(chanceOf(2)));
        metadata.put("altercations", String.valueOf(chanceOf(2)));
        metadata.put("casinoUsed", String.valueOf(chanceOf(50)));
        metadata.put("loyaltyLevel", randomLoyaltyLevel());
        TextSegment textSegment = TextSegment.from(body, metadata);
        exchange.getIn().setBody(textSegment);
    }

    private boolean chanceOf(int percentage) {
        return rnd.nextInt(100) < percentage;
    }

    private String randomLoyaltyLevel() {
        return loyaltyLevels.get(rnd.nextInt(loyaltyLevels.size()));
    }
}
