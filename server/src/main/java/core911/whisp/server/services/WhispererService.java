package core911.whisp.server.services;

import core911.whisp.core.model.MessageEnvelope;
import core911.whisp.server.util.EnvelopeStore;
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
    @Path("/welcome")
    @Produces(MediaType.TEXT_PLAIN)
    public String getGreeting() {
        log.trace("GET /welcome");
        return String.format(
                "Hello, Whisperer...\nWe have %d messages somewhere in the dark...\nWe use about %d bytes of memory to store these messages...\nBut Tss!.. We had never told you any of that...",
                envelopeStore.length(),
                envelopeStore.size()
        );
    }

    @GET
    @Path("/{topic}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Collection<MessageEnvelope> getByTopic(@PathParam("topic") short topic) {
        log.trace("GET /{}", topic);
        return envelopeStore.get(topic);
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Collection<MessageEnvelope> getDefault() {
        return getByTopic((short) 0);
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
    public void putDefault(List<MessageEnvelope> envelopes) {
        log.trace("POST /");
        validateEnvelopes(envelopes);
        envelopeStore.put(envelopes);
    }

    private void validateEnvelope(MessageEnvelope envelope) {
        if(envelope == null) {
            log.debug("no envelope: null");
            throw new BadRequestException("no envelope");
        }
        if(envelope.expired()) {
            log.debug("expired envelope: {}", envelope);
            throw new BadRequestException("envelope already expired: " + envelope.expiry);
        }
        if(envelope.ttl < 0) {
            log.debug("invalid TTL: {}", envelope);
            throw new BadRequestException("invalid TTL: " + envelope.ttl);
        }
        if(envelope.impliedInsertionTime() > Instant.now().getEpochSecond()) {
            log.debug("invalid implied insertion time: {}", envelope);
            throw new BadRequestException("invalid implied insertion time: " + envelope.impliedInsertionTime());
        }
        // TODO: implement real nonce validation
        if(envelope.nonce > 3141592653589793L) {
            log.debug("invalid nonce: {}", envelope);
            throw new BadRequestException("invalid nonce: " + envelope.nonce);
        }
        if(envelope.message == null) {
            log.debug("empty message: {}", envelope);
            throw new BadRequestException("empty message");
        }
        if(envelope.message.length != 0x100) {
            log.debug("invalid message size ({} bytes): {}", envelope.message.length, envelope);
            throw new BadRequestException("invalid message size: " + envelope.message.length);
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

