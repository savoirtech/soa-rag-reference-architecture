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
package com.savoir.soa.rag.ref.arch.agent;

import com.savoir.soa.rag.ref.arch.osgi.utils.OSGiSafeBgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import java.util.List;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

public class ChromaDataStore implements AgentDataStore {

    private String baseUrl;
    private String collectionName;
    private ChromaEmbeddingStore chromaEmbeddingStore;
    private EmbeddingModel embeddingModel = new OSGiSafeBgeSmallEnV15QuantizedEmbeddingModel();

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

    @Override
    public String search(String embeddingQuestion, String metadataKey, String metadataTarget) {

        Embedding queryEmbedding = embeddingModel.embed(embeddingQuestion).content();

        Filter metadataFilter = metadataKey(metadataKey).isEqualTo(metadataTarget);

        EmbeddingSearchRequest request = EmbeddingSearchRequest
                .builder()
                .queryEmbedding(queryEmbedding)
                .filter(metadataFilter)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = this.chromaEmbeddingStore.search(request);
        EmbeddingMatch<TextSegment> embeddingMatch = searchResult.matches().get(0);
        System.out.println("Found matches: " + searchResult.matches().size());
        return embeddingMatch.embedded().text();
    }

    @Override
    public String findRelevant(String embeddingQuestion) {

        Embedding queryEmbedding = embeddingModel.embed(embeddingQuestion).content();

        List<EmbeddingMatch<TextSegment>> relevant = this.chromaEmbeddingStore.findRelevant(queryEmbedding, 1);
        EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(0);

        System.out.println("Found relevant matches: " + relevant.size());
        return embeddingMatch.embedded().text();
    }

    @Override
    public String askQuestion(String question) {
        return "";
    }
}
