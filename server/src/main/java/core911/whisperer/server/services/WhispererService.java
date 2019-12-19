package core911.whisperer.server.services;

import core911.whisperer.server.resources.MessageEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * @author vgorin
 *         file created on 12/19/2019 5:02 PM
 */

@Path("/")
public class WhispererService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public String getGreeting() {
        return "Hello, Whisperer... Tss";
    }

    @GET
    @Path("/{topic}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<byte[]> getMessages(@PathParam("topic") int topic) {
        throw new ServerErrorException(501);
    }

    @POST
    @Path("/{topic}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void putByTopic(@PathParam("topic") int topic, List<MessageEnvelope> envelopes) {
        validateEnvelopes(envelopes);
        throw new ServerErrorException(501);
    }

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void putMessages(List<MessageEnvelope> envelopes) {
        validateEnvelopes(envelopes);
        throw new ServerErrorException(501);
    }

    private void validateEnvelope(MessageEnvelope envelope) {
        if(envelope == null) {
            log.trace("No envelope: null");
            throw new BadRequestException("No envelope");
        }
        if(envelope.expiry <= Instant.now().getEpochSecond()) {
            log.trace("Expired envelope: {}", envelope);
            throw new BadRequestException("Already expired!");
        }
        if(envelope.ttl < 0) {
            log.trace("Invalid TTL: {}", envelope);
            throw new BadRequestException("Invalid TTL!");
        }
        if(envelope.expiry - envelope.ttl > Instant.now().getEpochSecond()) {
            log.trace("Invalid implied insertion time: {}", envelope);
            throw new BadRequestException("Invalid implied insertion time! You will be punished!!!");
        }
        // TODO: implement real nonce validation
        if(envelope.nonce != 3141592653589793L) {
            log.trace("Invalid nonce: {}", envelope);
            throw new BadRequestException("Invalid nonce! Work more to find the correct nonce.");
        }
        if(envelope.message == null) {
            log.trace("Empty message: {}", envelope);
            throw new BadRequestException("Empty message!");
        }
        if(envelope.message.length != 0x100) {
            log.trace("Invalid message size ({} bytes): {}", envelope.message.length, envelope);
            throw new BadRequestException("Invalid message size!");
        }
    }

    private void validateEnvelopes(Collection<MessageEnvelope> envelopes) {
        if(envelopes == null || envelopes.isEmpty()) {
            log.trace("Empty request: no envelopes: {}", envelopes);
            throw new BadRequestException("Empty request!");
        }
        for(MessageEnvelope envelope: envelopes) {
            validateEnvelope(envelope);
        }
    }

}

