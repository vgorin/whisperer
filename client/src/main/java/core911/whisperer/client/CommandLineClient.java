package core911.whisperer.client;

import core911.whisperer.common.resources.MessageEnvelope;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Locale;

/**
 * @author vgorin
 *         file created on 12/19/2019 9:58 AM
 */

public class CommandLineClient {
    // TODO: add more names to these lists
    private static final String[] NAME_TITLE = {"Big", "Tall", "Small", "Deep", "Tiny"};
    private static final String[] NAME_FIRST = {"Juicy", "Tasty", "Salty", "Sweet", "Bitter", "Hot", "Cold", "Chilly", "Icy"};
    private static final String[] NAME_LAST = {"Apricot", "Banana", "Apple", "Carrot", "Potato", "Onion", "Pear", "Pineapple", "Octopus", "Ramp", "Cherry", "Strawberry", "Blackberry"};

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        if(new Locale("ru").getLanguage().equalsIgnoreCase(Locale.getDefault().getLanguage())) {
            System.out.println(
                    "  _____     _____                         \n" +
                    " |_   _| _ |_   _|                        \n" +
                    "   | |  | |  | |  ___ _ _______   _ _   _ \n" +
                    "   | |  | |  | | / _ \\ '_ \\   _| | | |_| |\n" +
                    "  _| |__| |__| ||  __/ | | | | |_| |  _  |\n" +
                    " |_______________\\___|_| |_|_|\\__, |_| |_|\n" +
                    "                               __/ |       \n" +
                    "                              |___/        ");
        }
        else {
            System.out.println(
                    " __          ___     _                              \n" +
                    " \\ \\        / / |   (_)                             \n" +
                    "  \\ \\  /\\  / /| |__  _ ___ _ __   ___ _ __ ___ _ __ \n" +
                    "   \\ \\/  \\/ / | '_ \\| / __| '_ \\ / _ \\ '__/ _ \\ '__|\n" +
                    "    \\  /\\  /  | | | | \\__ \\ |_) |  __/ | |  __/ |   \n" +
                    "     \\/  \\/   |_| |_|_|___/ .__/ \\___|_|  \\___|_|   \n" +
                    "                          | |                       \n" +
                    "                          |_|                       ");
        }
        System.out.println();

        Security.addProvider(new BouncyCastleProvider());
        // TODO: review hashing algorithm
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        byte[] password = readPassword("Please identify yourself with a password (at least 8 characters):");
        if(password.length < 8) {
            System.out.println("Password is too short!");
            System.exit(1);
        }
        // TODO: review password hashing idea
        password = messageDigest.digest(password);

        System.out.println();
        System.out.println("Thank you. We have hashed your password and it's no longer stored in the memory.");
        System.out.println(String.format("You are now known as %s. Other participants will see your messages signed by this name.", generateName(password)));
        System.out.println();
        System.out.println("Your messages are padded to protect the metadata and then encrypted using your password to protect the data inside.");
        System.out.println("No one except the recipient can look neither inside your messages nor observe them from outside.");
        System.out.println();
        System.out.println("...::: Welcome to the Dark! :::...");
        System.out.println();

        // TODO: derive shared encryption key without compromising the password and encrypt all messages


        WhispererClient client = new WhispererClient("http://localhost:45592/whisperer/");
        client.listenForMessages(new PrintWriter(System.out, true));
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String message;
        do {
            message = reader.readLine();
            // TODO: encrypt message here and only then send
            if(!client.sendMessage(message.getBytes())) {
                System.out.println("ERROR: message was not sent!");
            }
        }
        while(!"exit".equals(message));
        client.stopListenForMessages();
    }

    private static byte[] readPassword(String message) throws IOException {
        byte[] password;
        Console console = System.console();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        if(console != null) {
            password = new String(console.readPassword(message)).getBytes();
        }
        else {
            System.out.println(message);
            password = reader.readLine().getBytes();
        }
        return password;
    }

    private static String generateName(byte[] hash) {
        BigInteger i = new BigInteger(hash).abs();
        String title = NAME_TITLE[i.remainder(BigInteger.valueOf(NAME_TITLE.length)).intValue()];
        String first = NAME_FIRST[i.remainder(BigInteger.valueOf(NAME_FIRST.length)).intValue()];
        String last = NAME_LAST[i.remainder(BigInteger.valueOf(NAME_LAST.length)).intValue()];
        return String.format("%s %s %s", title, first, last);
    }

}
