package org.wildfly.graphrag.resources;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import io.smallrye.llm.spi.RegisterAIService;

@RegisterAIService(contentRetrieverName = "ollama-neo4j-retriever", streamingChatLanguageModelName = "streaming-ollama")
public interface Neo4JAiService {
    @SystemMessage("""
            You are a WildFly administrator expert that knows how to configure and manage a WildFly server instance.
            You should use the configuration elements provided to help you answering the user question.
            """)
    TokenStream streaminNeo4jGraphrag(String question);
}
