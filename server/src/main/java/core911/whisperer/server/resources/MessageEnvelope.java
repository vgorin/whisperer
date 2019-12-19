package core911.whisperer.server.resources;

import core911.whisperer.common.util.AbstractJson;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author vgorin
 *         file created on 12/19/2019 5:06 PM
 */

@XmlRootElement
public class MessageEnvelope extends AbstractJson {
    @XmlElement
    public int expiry;
    @XmlElement
    public short ttl;
    @XmlElement
    public short topic;
    @XmlElement
    public byte[] message;
    @XmlElement
    public long nonce;
}
