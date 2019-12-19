package core911.whisperer.server.services;

import core911.whisperer.server.resources.MessageEnvelope;
import core911.whisperer.server.util.EnvelopeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * @author vgorin
 *         file created on 12/19/2019 5:02 AM
 */

@Path("/")
public class WhispererService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    // TODO: replace with some kind of dependency injection
    private static final EnvelopeStore envelopeStore = new EnvelopeStore();

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public String getGreeting() {
        log.trace("GET /");
        return String.format(
                "Hello, Whisperer...\nWe have %d messages somewhere in the dark...\nWe use about %d bytes of memory to store these messages...\nBut Tss!.. We had never told you any of that...",
                envelopeStore.length(),
                envelopeStore.size()
        );
    }

    @GET
    @Path("/{topic}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Collection<MessageEnvelope> getMessages(@PathParam("topic") short topic) {
        log.trace("GET /{}", topic);
        return envelopeStore.get(topic);
    }

    @POST
    @Path("/{topic}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void putByTopic(@PathParam("topic") short topic, List<MessageEnvelope> envelopes) {
        log.trace("POST /{}", topic);
        validateEnvelopes(envelopes);
        envelopeStore.put(topic, envelopes);
    }

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void putMessages(List<MessageEnvelope> envelopes) {
        log.trace("POST /");
        validateEnvelopes(envelopes);
        envelopeStore.put(envelopes);
    }

    private void validateEnvelope(MessageEnvelope envelope) {
        if(envelope == null) {
            log.debug("No envelope: null");
            throw new BadRequestException("No envelope");
        }
        if(envelope.expired()) {
            log.debug("Expired envelope: {}", envelope);
            throw new BadRequestException("Already expired!");
        }
        if(envelope.ttl < 0) {
            log.debug("Invalid TTL: {}", envelope);
            throw new BadRequestException("Invalid TTL!");
        }
        if(envelope.impliedInsertionTime() > Instant.now().getEpochSecond()) {
            log.debug("Invalid implied insertion time: {}", envelope);
            throw new BadRequestException("Invalid implied insertion time! You will be punished!!!");
        }
        // TODO: implement real nonce validation
        if(envelope.nonce != 3141592653589793L) {
            log.debug("Invalid nonce: {}", envelope);
            throw new BadRequestException("Invalid nonce! Work more to find the correct nonce.");
        }
        if(envelope.message == null) {
            log.debug("Empty message: {}", envelope);
            throw new BadRequestException("Empty message!");
        }
        if(envelope.message.length != 0x100) {
            log.debug("Invalid message size ({} bytes): {}", envelope.message.length, envelope);
            throw new BadRequestException("Invalid message size!");
        }
    }

    private void validateEnvelopes(Collection<MessageEnvelope> envelopes) {
        if(envelopes == null || envelopes.isEmpty()) {
            log.debug("Empty request: no envelopes: {}", envelopes);
            throw new BadRequestException("Empty request!");
        }
        for(MessageEnvelope envelope: envelopes) {
            validateEnvelope(envelope);
        }
    }

}

