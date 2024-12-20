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
package com.savoir.soa.rag.ref.arch.impl;

import com.savoir.soa.rag.ref.arch.api.AgentService;
import com.savoir.soa.rag.ref.arch.agent.AgentDataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class AgentServiceImpl implements AgentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentServiceImpl.class);

    private AgentDataStore agentDataStore;

    public AgentServiceImpl(AgentDataStore agentDataStore) {
        this.agentDataStore = agentDataStore;
    }

    @Override
    @POST
    @Path("/ask")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response ask(String question) {
        LOGGER.info("Asked question: " + question);
        String response = "Error processing your request...";
        try {
            response = agentDataStore.askQuestion(question);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("Response: " + response);
        return Response.status(200).header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD").entity(response).build();
    }

    @Override
    @POST
    @Path("/search")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String search(String query) {
        LOGGER.info("User Query:: " + query);
        return agentDataStore.search("Which customer is most common?", "tenant", "savoir");
    }

    @Override
    @POST
    @Path("/findRelevant")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String findRelevant(String query) {
        LOGGER.info("User Query:: " + query);
        return agentDataStore.findRelevant("Whom has ordered abc widget?");
    }

    @GET
    @Path("/healthCheck")
    public Response healthCheck() {
        return Response.status(200).header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD").entity("").build();
    }

}
