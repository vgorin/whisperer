package core911.whisp.client;

import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * @author vgorin
 *         file created on 12/19/2019 9:58 AM
 */

public class CommandLineClient {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        Options options = new Options();
        options.addOption("e", "endpoint", true, "endpoint address");

/*
        // automatically generate the help statement:
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("whisperer", options );
*/

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        }
        catch(ParseException e) {
            System.out.println(e.getMessage());
            return;
        }

        String endpoint = cmd.getOptionValue("e", "http://core911.online/whisp/");

        ChatEngine engine = new ChatEngine(endpoint);
        if(!engine.testConnection()) {
            System.out.println("Could not connect to the network. Did you forget to pay for the network?");
            return;
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

        byte[] password = readPassword("Please identify yourself with a password (at least 8 characters):");
        if(password.length < 8) {
            System.out.println("Password is too short!");
            System.exit(1);
        }

        // TODO: review hashing algorithm
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        // TODO: review password hashing idea
        password = messageDigest.digest(password);
        String name = NameUtils.generateName(password);

        System.out.println();
        System.out.println("Thank you. We have hashed your password and destroyed it immediately.");
        System.out.println(String.format("You are now known as %s. Other participants will see your messages signed by this name.", name));
        System.out.println();
        System.out.println("Your messages are padded to protect the metadata and then encrypted using your password to protect the data inside.");
        System.out.println("No one except the recipient can look neither inside your messages nor observe them from outside.");
        System.out.println();
        System.out.println("...::: Welcome to the Dark! :::...");
        System.out.println();

        Runtime.getRuntime().addShutdownHook(new Thread(engine::shutdown));

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

}
