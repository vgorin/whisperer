package core911.whisperer.server.util;

import core911.whisperer.server.resources.MessageEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author vgorin
 * file created on 12/19/2019 7:09 AM
 */

public class EnvelopeStore {
    private final Logger log = LoggerFactory.getLogger(getClass());

    // TODO: replace this in-memory store with DB/Disk-store or make it configurable/adoptive
    private final Map<Short, Collection<MessageEnvelope>> envelopeStore = new ConcurrentHashMap<>();

    public void put(short topic, MessageEnvelope envelope) {
        envelope.topic = topic;
        put(envelope);
    }

    public void put(short topic, Collection<MessageEnvelope> envelopes) {
        for (MessageEnvelope envelope : envelopes) {
            put(topic, envelope);
        }
    }

    public void put(MessageEnvelope envelope) {
        short topic = envelope.topic;
        envelopeStore.putIfAbsent(topic, ConcurrentHashMap.newKeySet());
        Collection<MessageEnvelope> envelopes = envelopeStore.get(topic);
        log.trace("envelope store has {} envelopes in {} topic, adding new envelope", envelopes.size(), topic);
        envelopes.add(envelope);
        log.trace("envelope store has now {} envelopes in {} topic", envelopes.size(), topic);
    }

    public void put(Collection<MessageEnvelope> envelopes) {
        for (MessageEnvelope envelope : envelopes) {
            put(envelope);
        }
    }

    public Collection<MessageEnvelope> get(short topic) {
        cleanup(topic);
        return envelopeStore.get(topic);
    }

    /**
     *
     * @return occupied memory size in bytes
     */
    public long size() {
        // TODO: inaccurate implementation, implement this properly
        return length() * MessageEnvelope.MAX_SIZE;
    }

    /**
     *
     * @return total number of envelopes currently stored
     */
    public long length() {
        // TODO: how can we optimize this code using FP in Java 8+?
        long len = 0;
        for(Map.Entry<Short, Collection<MessageEnvelope>> entry: envelopeStore.entrySet()) {
            len += entry.getValue().size();
        }
        return len;
    }

    /**
     * Removes expired envelopes from the storage
     */
    public void gc() {
        log.trace("gc is cleaning up now");
        for(Short topic: envelopeStore.keySet()) {
            cleanup(topic);
        }
    }

    /**
     * Removes expired envelopes for a given topic (key)
     * @param topic a topic to remove expired envelopes for
     */
    private void cleanup(short topic) {
        log.trace("cleaning up topic {}", topic);
        Collection<MessageEnvelope> envelopes = envelopeStore.get(topic);
        for(MessageEnvelope envelope: envelopes) {
            if(envelope.expired()) {
                log.trace("removing expired envelope {}", envelope);
                envelopes.remove(envelope);
            }
        }
        if(envelopes.isEmpty()) {
            log.trace("removing empty topic {}", topic);
            envelopeStore.remove(topic);
        }
    }

    /**
     * Removes all the envelopes from the storage
     */
    public void clear() {
        log.trace("clearing the storage");
        envelopeStore.clear();
    }
}
