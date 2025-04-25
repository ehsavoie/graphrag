package org.wildfly.graphrag.resources;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import java.io.StringReader;
import org.jboss.logging.Logger;

/**
 *
 * @author
 */
@Path("/neo4j")
public class RestResource {

    private static final Logger LOGGER = Logger.getLogger(RestResource.class);

    @Inject
    private Neo4JAiService aiService;

    @GET
    public Response ping() {
        return Response
                .ok("ping Jakarta EE")
                .build();
    }

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @Path("/streaming-chat")
    public void streamingChatWithAssistant(@Context Sse sse, @Context SseEventSink sseEventSink,
            @HeaderParam(HttpHeaders.LAST_EVENT_ID_HEADER) @DefaultValue("-1") int lastReceivedId,
            @QueryParam("question") String question) throws InterruptedException {
        final int lastEventId;
        if (lastReceivedId != -1) {
            lastEventId = lastReceivedId + 1;
        } else {
            lastEventId = 1;
        }
        OutboundSseEvent.Builder eventBuilder = sse.newEventBuilder();
        aiService.streaminNeo4jGraphrag(question)
                .onPartialResponse(partialResponse -> {
                    OutboundSseEvent sseEvent = eventBuilder
                            .name("token")
                            .id(String.valueOf(lastEventId + 1))
                            .mediaType(MediaType.TEXT_PLAIN_TYPE)
                            .data(partialResponse.replace("\n", "<br/>"))
                            .reconnectDelay(3000)
                            .comment("This is a token from the llm")
                            .build();
                    sseEventSink.send(sseEvent);
                })
                .onToolExecuted(tooExecution -> {
                    LOGGER.info("Tool " + tooExecution.request().name() + " was called with result " + tooExecution.result());
                })
                .onCompleteResponse(chatResponse -> {
                    OutboundSseEvent sseEvent = eventBuilder
                            .name("token")
                            .id(String.valueOf(lastEventId + 1))
                            .mediaType(MediaType.TEXT_PLAIN_TYPE)
                            .data("end-data-token")
                            .reconnectDelay(3000)
                            .comment("This is a token from the llm")
                            .build();
                    sseEventSink.send(sseEvent)
                            .whenComplete((event, throwable) -> {
                                sseEventSink.close();
                            });
                })
                .onError(error -> {
                    LOGGER.error("Error processing question \"" + question + "\"", error);
                    JsonObject message = Json.createReader(new StringReader(error.getMessage())).readObject().getJsonObject("error");
                    LOGGER.error("Error sending error " + message.toString());
                    OutboundSseEvent sseEvent = eventBuilder
                            .name("token")
                            .id(String.valueOf(lastEventId + 1))
                            .mediaType(MediaType.TEXT_PLAIN_TYPE)
                            .data(message.toString())
                            .reconnectDelay(3000)
                            .comment("This is an error from the llm")
                            .build();
                    sseEventSink.send(sseEvent);
                })
                .start();
    }

}
