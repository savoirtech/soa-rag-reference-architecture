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

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;

public class ChromaDataStore {

    private String baseUrl;
    private String collectionName;
    private ChromaEmbeddingStore chromaEmbeddingStore;

    public ChromaDataStore(String baseUrl, String collectionName) {
        this.baseUrl = baseUrl;
        this.collectionName = collectionName;
        this.chromaEmbeddingStore = ChromaEmbeddingStore.builder()
                .baseUrl(baseUrl)
                .logRequests(true)
                .logResponses(true)
                .collectionName(collectionName)
                .build();
    }

    void add(Embedding embedding, TextSegment textSegment) {
        chromaEmbeddingStore.add(embedding, textSegment);
    }

}
