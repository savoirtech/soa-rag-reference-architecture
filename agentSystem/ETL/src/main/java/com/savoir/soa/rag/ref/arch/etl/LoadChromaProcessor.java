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

import com.savoir.soa.rag.ref.arch.osgi.utils.OSGiSafeBgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Loads data into Chroma.
 *
 * In Camel 4.5+ use the camel-langchain4j-embeddings component.
 */
public class LoadChromaProcessor implements Processor {

    private ChromaDataStore chromaDataStore;

    public LoadChromaProcessor(ChromaDataStore chromaDataStore) {
        this.chromaDataStore = chromaDataStore;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        TextSegment textSegment = exchange.getIn().getBody(TextSegment.class);
        EmbeddingModel embeddingModel = new OSGiSafeBgeSmallEnV15QuantizedEmbeddingModel();
        Embedding embedding = embeddingModel.embed(textSegment).content();
        chromaDataStore.add(embedding, textSegment);
    }
}
