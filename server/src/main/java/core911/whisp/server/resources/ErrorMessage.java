package core911.whisp.server.resources;

import javax.ws.rs.ClientErrorException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author vgorin
 *         file created on 9/4/2019 7:09 AM
 */

@XmlRootElement
public class ErrorMessage {
    @XmlElement
    public int code;

    @XmlElement
    public String description;

    public ErrorMessage(ClientErrorException e) {
        code = e.getResponse().getStatus();
        description = e.getMessage();
    }
}
