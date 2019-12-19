package core911.whisperer.server.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author vgorin
 * file created on 2019-12-19 05:06
 */

@XmlRootElement
public class MessageEnvelope {
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
