package core911.whisperer.server.services;

import core911.whisperer.server.resources.MessageEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author vgorin
 * file created on 2019-12-19 05:02
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
        throw new ServerErrorException(501);
    }

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void putMessages(List<MessageEnvelope> envelopes) {
        throw new ServerErrorException(501);
    }

}

