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
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.localai.LocalAiChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import dev.langchain4j.rag.content.aggregator.DefaultContentAggregator;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

public class ChromaDataStore implements AgentDataStore {

    private String baseUrl;
    private String collectionName;
    private ChromaEmbeddingStore chromaEmbeddingStore;
    private EmbeddingModel embeddingModel = (EmbeddingModel) new OSGiSafeBgeSmallEnV15QuantizedEmbeddingModel();
    private static final Logger LOGGER = LoggerFactory.getLogger(ChromaDataStore.class);

    private CruiseAssistant assistant;

    public ChromaDataStore(String baseUrl, String collectionName) {
        this.baseUrl = baseUrl;
        this.collectionName = collectionName;
        this.chromaEmbeddingStore = ChromaEmbeddingStore.builder()
                .baseUrl(baseUrl)
                .logRequests(true)
                .logResponses(true)
                .collectionName(collectionName)
                .build();
        assistant = createAssistant("cruise-ship.txt");
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
        LOGGER.info("Found matches: " + searchResult.matches().size());
        return embeddingMatch.embedded().text();
    }

    @Override
    public String findRelevant(String embeddingQuestion) {

        Embedding queryEmbedding = embeddingModel.embed(embeddingQuestion).content();

        List<EmbeddingMatch<TextSegment>> relevant = this.chromaEmbeddingStore.findRelevant(queryEmbedding, 1);
        EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(0);

        LOGGER.info("Found relevant matches: " + relevant.size());
        return embeddingMatch.embedded().text();
    }

    @Override
    public String askQuestion(String question) {
        return assistant.answer(question);
    }

    private CruiseAssistant createAssistant(String documentPath) {

        Document document = loadDocument(toPath(documentPath), new TextDocumentParser());

        System.setProperty("OPT_OUT_TRACKING", "true");
        System.setProperty("ai.djl.offline", "true");

        EmbeddingModel embeddingModel = (EmbeddingModel) new OSGiSafeBgeSmallEnV15QuantizedEmbeddingModel();

        EmbeddingStore<TextSegment> embeddingStore1 = new InMemoryEmbeddingStore<>();

        EmbeddingStore<TextSegment> embeddingStore2 = ChromaEmbeddingStore
                .builder()
                .baseUrl("http://localhost:8000")
                .collectionName("soa-rag-ref-arch")
                .logRequests(true)
                .logResponses(true)
                .build();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 0))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore1)
                .build();

        ingestor.ingest(document);

        ChatLanguageModel chatLanguageModel = LocalAiChatModel.builder()
                .baseUrl("http://localhost:8080")
                .modelName("gpt-4o")
                .maxRetries(3)
                .timeout(Duration.ofSeconds(30))
                .temperature(0.7)
                .maxTokens(100)
                .logRequests(true)
                .logResponses(true)
                .build();

        QueryTransformer queryTransformer = new CompressingQueryTransformer(chatLanguageModel);

        ContentRetriever cruiseInformationRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore1)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.6)
                .build();

        Filter cruiseFilter = metadataKey("tenant").isEqualTo("savoir");

        ContentRetriever reservationInformationRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore2)
                .embeddingModel(embeddingModel)
                .filter(cruiseFilter)
                .displayName("default")
                .build();

        // Let's create a query route that decides which content is more relevant
//        Map<ContentRetriever, String> retrieverToDescription = new HashMap<>();
//        retrieverToDescription.put(cruiseInformationRetriever, "details about the cruse ship");
//        retrieverToDescription.put(reservationInformationRetriever, "passenger reservation details");
//
//        PromptTemplate promptTemplate = PromptTemplate.from("Based on the user query, use data source(s) to retrieve relevant information from the following options:\n{{options}}\nIt is very important that your answer contains cruise information and reservation data!\nUser query: {{query}}");
//        QueryRouter queryRouter = LanguageModelQueryRouter.builder()
//                .chatLanguageModel(chatLanguageModel)
//                .retrieverToDescription(retrieverToDescription)
//                .promptTemplate(promptTemplate)
//                .build();

        QueryRouter queryRouter = new DefaultQueryRouter(cruiseInformationRetriever, reservationInformationRetriever);

        Map<Query, Collection<List<Content>>> map = new HashMap<>();
        Query query = new Query("Is Gold loyalty level?");
        Embedding queryEmbedding = embeddingModel.embed("gold loyalty level").content();
        List<EmbeddingMatch<TextSegment>> relevant = this.chromaEmbeddingStore.findRelevant(queryEmbedding, 10);
        LOGGER.info("Found relevant matches: " + relevant.size());
        Collection<List<Content>> ourContent = findingsToContent(relevant);
        map.put(query, ourContent);
        ContentAggregator contentAggregator = new DefaultContentAggregator();
        contentAggregator.aggregate(map);

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .queryRouter(queryRouter)
                .contentAggregator(contentAggregator)
                .build();

        return AiServices.builder(CruiseAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    private static Collection<List<Content>> findingsToContent(List<EmbeddingMatch<TextSegment>> relevant) {
        Collection<List<Content>> result = new ArrayList<>();
        List<Content> ourContent = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> embeddingMatch : relevant) {
            Content content = new Content(embeddingMatch.embedded());
            ourContent.add(content);
        }
        result.add(ourContent);
        return result;
    }

    private static Path toPath(String relativePath) {
        try {
            File file = File.createTempFile("apache-karaf", ".txt");
            InputStream inputStream = new java.io.BufferedInputStream(ChromaDataStore.class.getClassLoader().getResourceAsStream(relativePath));
            try(OutputStream outputStream = new FileOutputStream(file)){
                IOUtils.copy(inputStream, outputStream);
            } catch (IOException e) {
                // handle exception here
            }
            return Paths.get(file.toURI());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
