package core911.whisp.server.services;

import core911.whisp.server.resources.ErrorMessage;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author vgorin
 *         file created on 2019-09-04 07:07
 */

@Provider
public class ClientExceptionMapper implements ExceptionMapper<ClientErrorException> {
    @Override
    public Response toResponse(ClientErrorException exception) {
        return Response.status(exception.getResponse().getStatus()).entity(new ErrorMessage(exception)).type(MediaType.APPLICATION_JSON).build();
    }
}
