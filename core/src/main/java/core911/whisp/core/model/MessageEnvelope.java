package core911.whisp.core.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * @author vgorin
 *         file created on 12/19/2019 5:06 AM
 */

@XmlRootElement
public class MessageEnvelope implements Serializable {
    // TODO: this calculation is not accurate, actual memory consumption can be lower (see object and array headers)
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

    @XmlElement(name = "expired")
    public boolean isExpired() {
        return expiry <= System.currentTimeMillis() / 1000;
    }

    @XmlElement(name = "implied_insertion_time")
    public int getImpliedInsertionTime() {
        return expiry - ttl;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        MessageEnvelope that = (MessageEnvelope) o;
        return nonce == that.nonce &&
                expiry == that.expiry &&
                ttl == that.ttl &&
                topic == that.topic &&
                Arrays.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(nonce, expiry, ttl, topic);
        result = 31 * result + Arrays.hashCode(message);
        return result;
    }

    @Override
    public String toString() {
        return toPrettyJson();
    }

    public String toJson() {
        return JsonUtil.toJson(this);
    }

    public String toPrettyJson() {
        return JsonUtil.toPrettyJson(this);
    }

    public static String toJson(Collection<MessageEnvelope> envelopes) {
        return JsonUtil.toJson(envelopes);
    }

    public static Collection<MessageEnvelope> fromJson(String json) {
        return Arrays.asList(JsonUtil.parseJson(json, MessageEnvelope[].class));
    }
}
