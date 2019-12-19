package core911.whisperer.common.resources;

import core911.whisperer.common.util.AbstractJson;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author vgorin
 *         file created on 12/19/2019 5:06 AM
 */

@XmlRootElement
public class MessageEnvelope extends AbstractJson {
    // TODO: this implementation is not accurate, actual memory consumption can be lower (see object and array headers)
    /**
     * object header (2 words – 16 bytes), https://stackoverflow.com/questions/26357186/what-is-in-java-object-header
     * nonce field (8 bytes)
     * fields – expiry (4 bytes), ttl (2 bytes), topic (2 bytes)
     * message array header (16 bytes)
     * array contents size - 256 bytes
     */
    public static final int MAX_SIZE = 304;

    /* All the elements are ordered as in JVM */

    /* long/double, int/float, short/char, byte/boolean */
    @XmlElement
    public long nonce;
    @XmlElement
    public int expiry;
    @XmlElement
    public short ttl;
    @XmlElement
    public short topic;

    /* references */
    @XmlElement
    public byte[] message;


    public boolean expired() {
        return expiry <= System.currentTimeMillis() / 1000;
    }


    public int impliedInsertionTime() {
        return expiry - ttl;
    }

}
