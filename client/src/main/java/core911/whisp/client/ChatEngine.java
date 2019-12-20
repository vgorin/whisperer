package core911.whisp.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author vgorin
 *         file created on 12/20/2019 5:46 AM
 */

public class ChatEngine {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final WhispererClient client;

    private final AtomicBoolean listenUpstream = new AtomicBoolean();
    private final AtomicBoolean listenDownstream = new AtomicBoolean();

    private final ExecutorService senderService = Executors.newCachedThreadPool();

    public ChatEngine(String endpoint) {
        this(new WhispererClient(endpoint));
    }

    public ChatEngine(URI endpoint) {
        this(new WhispererClient(endpoint));
    }

    public ChatEngine(WhispererClient client) {
        this.client = client;
    }

    public boolean testConnection() {
        try {
            return client.getWelcomeMessage() != null;
        }
        catch(Exception e) {
            return false;
        }
    }

    public void listenUpstream(String nickname, InputStream in, PrintStream out) {
        if(listenUpstream.compareAndSet(false, true)) {
            Thread worker = new Thread(() -> {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                out.println("ChatEngine: upstream connected");
                while(listenUpstream.get()) {
                    try {
                        String line = reader.readLine();
                        if(line == null) {
                            log.warn("null user input");
                            continue;
                        }
                        if(line.startsWith("/")) {
                            log.trace("processing user command {}", line);
                            if("/exit".equals(line)) {
                                out.println("ChatEngine: shutting down the engine");
                                shutdown();
                            }
                            else if("/help".equals(line)) {
                                out.println("ChatEngine: use /exit command to exit the chat");
                            }
                            else {
                                out.println("ChatEngine: unknown command " + line);
                            }
                        }
                        else {
                            log.trace("sending user message {}", line);
                            // TODO: encrypt the message
                            senderService.submit(() -> {
                                try {
                                    client.sendMessage(String.format("%s: %s", nickname, line));
                                }
                                catch(IOException e) {
                                    out.println("ChatEngine: couldn't deliver message " + line);
                                    log.warn("sending a message failed", e);
                                }
                            });
                        }
                    }
                    catch(IOException e) {
                        log.warn("I/O error reading user input", e);
                    }
                }
                out.println("ChatEngine: upstream disconnected");
            });
            worker.start();
        }
    }

    public void listenDownstream(PrintStream out) {
        if(listenDownstream.compareAndSet(false, true)) {
            Thread worker = new Thread(() -> {
                out.println("ChatEngine: downstream connected");
                while(listenDownstream.get()) {
                    long t = System.currentTimeMillis();

                    try {
                        byte[][] messages = client.downloadNewMessages();
                        if(messages != null) {
                            for(byte[] message: messages) {
                                if(message != null) {
                                    // TODO: decrypt the message
                                    out.println(new String(message));
                                }
                            }
                        }
                    }
                    catch(IOException e) {
                        out.println("ChatEngine: couldn't download new message(s)");
                        log.warn("downloading new messages failed", e);
                    }

                    sleepUntil(t + 247, listenDownstream);
                }
                out.println("ChatEngine: downstream disconnected");
            });
            worker.start();
        }
    }

    public void shutdown() {
        listenUpstream.set(false);
        listenDownstream.set(false);
    }

    private void sleepUntil(long until, AtomicBoolean condition) {
        while(condition.get() && System.currentTimeMillis() < until) {
            try {
                Thread.sleep(50);
            }
            catch(InterruptedException e) {
                log.trace("sleep interrupted", e);
            }
        }
    }
}
