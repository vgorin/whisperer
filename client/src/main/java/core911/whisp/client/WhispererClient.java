package core911.whisp.client;

import core911.whisp.core.model.MessageEnvelope;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author vgorin
 *         file created on 12/20/2019 4:33 AM
 */

public class WhispererClient {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final URI endpoint;

    private final ConcurrentMap<Integer, Short> knownMessages = new ConcurrentHashMap<>();

    public WhispererClient(String endpoint) {
        try {
            this.endpoint = new URI(endpoint);
        }
        catch(URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public WhispererClient(URI endpoint) {
        this.endpoint = endpoint;
    }

    public void sendMessage(short topic, String message) throws IOException {
        sendMessage(topic, message.getBytes());
    }

    public void sendMessage(short topic, byte[] message) throws IOException {
        Collection<MessageEnvelope> envelopes = prepareMessage(topic, message);
        log.trace("sending the messages {}", envelopes);
        markAsKnown(envelopes);
        httpSend(envelopes);
    }

    public void sendMessage(String message) throws IOException {
        sendMessage((short) 0, message);
    }

    public void sendMessage(byte[] message) throws IOException {
        sendMessage((short) 0, message);
    }

    public byte[][] downloadNewMessages() throws IOException {
        return downloadNewMessages((short) 0);
    }

    public byte[][] downloadNewMessages(short topic) throws IOException {
        Collection<MessageEnvelope> envelopes = downloadAllEnvelopes(topic, false);
        if(envelopes == null) {
            return null;
        }

        LinkedList<byte[]> result = new LinkedList<>();
        for(MessageEnvelope envelope: envelopes) {
            if(knownMessages.putIfAbsent(envelope.hashCode(), topic) == null) {
                result.add(removePadding(envelope.message));
            }
        }
        return result.toArray(new byte[result.size()][]);
    }

    public byte[][] downloadAllMessages() throws IOException {
        return downloadAllMessages((short) 0);
    }

    public byte[][] downloadAllMessages(short topic) throws IOException {
        Collection<MessageEnvelope> envelopes = downloadAllEnvelopes(topic, true);
        if(envelopes == null) {
            return null;
        }

        byte[][] result = new byte[envelopes.size()][];
        int i = 0;
        for(MessageEnvelope envelope: envelopes) {
            result[i++] = removePadding(envelope.message);
        }
        return result;
    }

    private Collection<MessageEnvelope> downloadAllEnvelopes(short topic, boolean markAsKnown) throws IOException {
        Collection<MessageEnvelope> envelopes = httpReceive(topic);
        if(markAsKnown && envelopes != null && !envelopes.isEmpty()) {
            for(MessageEnvelope envelope: envelopes) {
                knownMessages.put(envelope.hashCode(), envelope.topic);
            }
        }
        return envelopes;
    }

    private Collection<MessageEnvelope> prepareMessage(short topic, byte[] message) {
        MessageEnvelope envelope = new MessageEnvelope();
        envelope.nonce = (long) (Math.random() * 3141592653589793L);
        envelope.expiry = (int) (System.currentTimeMillis() / 1000 + 120);
        envelope.ttl = 300;
        envelope.topic = topic;
        envelope.message = addPadding(message);

        return new LinkedList<MessageEnvelope>() {{
            add(envelope);
        }};
    }

    private byte[] addPadding(byte[] msg) {
        byte[] padded = new byte[256];
        System.arraycopy(msg, 0, padded, 0, Math.min(msg.length, 255));
        padded[255] = (byte) msg.length;
        return padded;
    }

    private byte[] removePadding(byte[] msg) {
        int length = 0xFF & msg[255];
        byte[] original = new byte[length];
        System.arraycopy(msg, 0, original, 0, length);
        return original;
    }

    private void markAsKnown(Collection<MessageEnvelope> envelopes) {
        for(MessageEnvelope envelope: envelopes) {
            knownMessages.put(envelope.hashCode(), envelope.topic);
        }
    }

    public void httpSend(Collection<MessageEnvelope> envelopes) throws IOException {
        int sessionId = (int) (Math.random() * 65536);
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            URI endpoint = this.endpoint;
            HttpPost request = new HttpPost(endpoint);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");
            String json = MessageEnvelope.toJson(envelopes);
            request.setEntity(new StringEntity(json));

            log.trace("[{}] POST {}\n\n{}", sessionId, endpoint, json);
            try(CloseableHttpResponse response = client.execute(request)) {
                StatusLine status = response.getStatusLine();
                log.debug("[{}] {}", sessionId, status);
                if(status.getStatusCode() != 204) {
                    throw new IOException(status.toString());
                }
            }
        }
    }

    public Collection<MessageEnvelope> httpReceive(short topic) throws IOException {
        int sessionId = (int) (Math.random() * 65536);
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            URI endpoint = this.endpoint.resolve(String.valueOf(topic));
            HttpGet request = new HttpGet(endpoint);
            request.setHeader("Accept", "application/json");

            log.trace("[{}] GET {}", sessionId, endpoint);
            try(CloseableHttpResponse response = client.execute(request)) {
                String json = readOkResponse(sessionId, response);
                log.trace("[{}] json received: {}", sessionId, json);
                if(json == null) {
                    return null;
                }
                try {
                    Collection<MessageEnvelope> envelopes = MessageEnvelope.fromJson(json);
                    log.trace("[{}] json parsed: {}", sessionId, envelopes);
                    return envelopes;
                }
                catch(Exception e) {
                    log.debug("[{}] could't parse json: {}", sessionId, json, e);
                    throw new IOException(e);
                }
            }
        }
    }

    public String getWelcomeMessage() throws IOException {
        int sessionId = (int) (Math.random() * 65536);
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            URI endpoint = this.endpoint.resolve("welcome");
            HttpGet request = new HttpGet(endpoint);

            log.trace("[{}] GET {}", sessionId, endpoint);
            try(CloseableHttpResponse response = client.execute(request)) {
                return readOkResponse(sessionId, response);
            }
        }
    }

    private String readOkResponse(int sessionId, HttpResponse response) throws IOException {
        StatusLine status = response.getStatusLine();
        log.debug("[{}] {}", sessionId, status);
        if(status.getStatusCode() == 204) {
            return null;
        }
        if(status.getStatusCode() != 200) {
            throw new IOException(status.toString());
        }

        HttpEntity entity = response.getEntity();
        try {
            return EntityUtils.toString(entity);
        }
        catch(IOException e) {
            log.debug("[{}] error reading response", sessionId, e);
            throw e;
        }
        finally {
            EntityUtils.consume(entity);
        }
    }
}
