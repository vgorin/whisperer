package core911.whisp.client;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * @author vgorin
 *         file created on 12/19/2019 9:58 AM
 */

public class CommandLineClient {
    // TODO: add more names to these lists
    private static final String[] NAME_TITLE = {"Big", "Tall", "Small", "Deep", "Tiny"};
    private static final String[] NAME_FIRST = {"Juicy", "Tasty", "Salty", "Sweet", "Bitter", "Hot", "Cold", "Chilly", "Icy"};
    private static final String[] NAME_LAST = {"Apricot", "Banana", "Apple", "Carrot", "Potato", "Onion", "Pear", "Octopus", "Ramp", "Cherry", "Basil", "Garlic", "Lime", "Pea"};

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        ChatEngine engine = new ChatEngine("http://localhost:45592/whisp/");
        if(!engine.testConnection()) {
            System.out.println("Could not connect to the network. Did you forget to pay for the network?");
            System.exit(-1);
        }

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
        System.out.println("(c) 2019 CORE911. Developed with *LOVE* by CORE911 Team.");
        System.out.println();

        // TODO: review hashing algorithm
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        byte[] password = readPassword("Please identify yourself with a password (at least 8 characters):");
        if(password.length < 8) {
            System.out.println("Password is too short!");
            System.exit(1);
        }
        // TODO: review password hashing idea
        password = messageDigest.digest(password);
        String name = generateName(password);

        System.out.println();
        System.out.println("Thank you. We have hashed your password and it's no longer stored in the memory.");
        System.out.println(String.format("You are now known as %s. Other participants will see your messages signed by this name.", name));
        System.out.println();
        System.out.println("Your messages are padded to protect the metadata and then encrypted using your password to protect the data inside.");
        System.out.println("No one except the recipient can look neither inside your messages nor observe them from outside.");
        System.out.println();
        System.out.println("...::: Welcome to the Dark! :::...");
        System.out.println();

        engine.listenDownstream(System.out);
        engine.listenUpstream(name, System.in, System.out);
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
