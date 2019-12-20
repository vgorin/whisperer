package core911.whisp.client;

import core911.whisp.core.model.MessageEnvelope;
import org.apache.http.HttpEntity;
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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author vgorin
 *         file created on 12/19/2019 1:29 PM
 */

public class WhispererClient {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AtomicBoolean listening = new AtomicBoolean(false);

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ConcurrentMap<Long, Long> knownNonces = new ConcurrentHashMap<>();

    private final String endpoint;
    private static final short DEFAULT_TOPIC = 314;

    public WhispererClient(String endpoint) {
        this.endpoint = endpoint;
    }

    private Collection<MessageEnvelope> putIntoEnvelopes(byte[] message) {
        Collection<MessageEnvelope> envelopes = new LinkedList<>();
        envelopes.add(putIntoEnvelope(message));
        return envelopes;
    }

    private MessageEnvelope putIntoEnvelope(byte[] message) {
        MessageEnvelope envelope = new MessageEnvelope();
        envelope.nonce = (long) (Math.random() * 3141592653589793L);
        envelope.expiry = (int) (System.currentTimeMillis() / 1000 + 3600);
        envelope.ttl = 7200;
        envelope.topic = DEFAULT_TOPIC;
        envelope.message = new byte[256];
        System.arraycopy(message, 0, envelope.message, 0, Math.min(message.length, 256));
        return envelope;
    }

    private void sendEnvelopes(Collection<MessageEnvelope> envelopes) throws IOException {
        log.trace("sending envelope {}", envelopes);
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(endpoint);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");
            request.setEntity(new StringEntity(MessageEnvelope.toJson(envelopes)));

            try(CloseableHttpResponse response = client.execute(request)) {
                StatusLine status = response.getStatusLine();
                if(status.getStatusCode() != 204) {
                    log.trace("{}", status);
                }
                else {
                    for(MessageEnvelope envelope: envelopes) {
                        knownNonces.putIfAbsent(envelope.nonce, 2L);
                    }
                }
            }
            catch(IOException e) {
                log.debug("I/O error reading response", e);
                throw e;
            }
        }
        catch(IOException e) {
            log.debug("I/O error sending request", e);
            throw e;
        }
    }

    private Collection<MessageEnvelope> readEnvelopes(short topic) throws IOException {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(String.format("%s%d", endpoint, topic));
            request.setHeader("Accept", "application/json");
            try(CloseableHttpResponse response = client.execute(request)) {
                StatusLine status = response.getStatusLine();
                if(response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    try {
                        String json = EntityUtils.toString(entity);
                        try {
                            return MessageEnvelope.fromJson(json);
                        }
                        catch(Exception e) {
                            log.debug("malformed response", e);
                            throw new IOException(e);
                        }
                    }
                    finally {
                        EntityUtils.consume(entity);
                    }
                }
                else {
                    log.debug("{}", status);
                    return Collections.emptyList();
                }
            }
            catch(IOException e) {
                log.debug("I/O error reading response");
                throw e;
            }
        }
        catch(IOException e) {
            log.debug("I/O error sending request", e);
            throw e;
        }
    }

    public boolean sendMessage(byte[] message) {
        Collection<MessageEnvelope> envelopes = putIntoEnvelopes(message);
        try {
            sendEnvelopes(envelopes);
            return true;
        }
        catch(IOException e) {
            return false;
        }
    }

    public void listenForMessages(PrintWriter w) {
        listening.set(true);
        executorService.submit(() -> {
            while(listening.get()) {
                try {
                    Collection<MessageEnvelope> envelopes = readEnvelopes(DEFAULT_TOPIC);
                    if(envelopes != null) {
                        for(MessageEnvelope envelope : envelopes) {
                            if(knownNonces.putIfAbsent(envelope.nonce, 1L) == null) {
                                log.trace("new envelope {}", envelope);
                                w.println(new String(envelope.message));
                            }
                        }
                    }
                }
                catch(IOException e) {
                    log.debug("could not read new envelopes from the server", e);
                    w.println("ERROR: could not parse server response");
                }
                try {
                    Thread.sleep(247);
                }
                catch(InterruptedException e) {
                    log.trace("interrupted", e);
                }
            }
        });
    }

    public void stopListenForMessages() {
        listening.set(false);
    }

}
