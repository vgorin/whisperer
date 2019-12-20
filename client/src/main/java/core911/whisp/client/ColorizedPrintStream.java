package core911.whisp.client;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author vgorin
 * file created on 2019-12-20 12:31
 */

public class ColorizedPrintStream extends PrintStream {
    private static final String ANSI_RESET  = "\u001B[0m";

    private static final String ANSI_BLACK  = "\u001B[30m";
    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE   = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN   = "\u001B[36m";
    private static final String ANSI_WHITE  = "\u001B[37m";

    private static final String ANSI_BRIGHT_BLACK  = "\u001B[90m";
    private static final String ANSI_BRIGHT_RED    = "\u001B[91m";
    private static final String ANSI_BRIGHT_GREEN  = "\u001B[92m";
    private static final String ANSI_BRIGHT_YELLOW = "\u001B[93m";
    private static final String ANSI_BRIGHT_BLUE   = "\u001B[94m";
    private static final String ANSI_BRIGHT_PURPLE = "\u001B[95m";
    private static final String ANSI_BRIGHT_CYAN   = "\u001B[96m";
    private static final String ANSI_BRIGHT_WHITE  = "\u001B[97m";

    private static final String[] FOREGROUNDS = {
            ANSI_BLACK, ANSI_RED, ANSI_GREEN, ANSI_YELLOW,
            ANSI_BLUE, ANSI_PURPLE, ANSI_CYAN, ANSI_WHITE,
            ANSI_BRIGHT_BLACK, ANSI_BRIGHT_RED, ANSI_BRIGHT_GREEN, ANSI_BRIGHT_YELLOW,
            ANSI_BRIGHT_BLUE, ANSI_BRIGHT_PURPLE, ANSI_BRIGHT_CYAN, ANSI_BRIGHT_WHITE
    };

    private static final String ANSI_BG_BLACK  = "\u001B[40m";
    private static final String ANSI_BG_RED    = "\u001B[41m";
    private static final String ANSI_BG_GREEN  = "\u001B[42m";
    private static final String ANSI_BG_YELLOW = "\u001B[43m";
    private static final String ANSI_BG_BLUE   = "\u001B[44m";
    private static final String ANSI_BG_PURPLE = "\u001B[45m";
    private static final String ANSI_BG_CYAN   = "\u001B[46m";
    private static final String ANSI_BG_WHITE  = "\u001B[47m";

    private static final String ANSI_BRIGHT_BG_BLACK  = "\u001B[100m";
    private static final String ANSI_BRIGHT_BG_RED    = "\u001B[101m";
    private static final String ANSI_BRIGHT_BG_GREEN  = "\u001B[102m";
    private static final String ANSI_BRIGHT_BG_YELLOW = "\u001B[103m";
    private static final String ANSI_BRIGHT_BG_BLUE   = "\u001B[104m";
    private static final String ANSI_BRIGHT_BG_PURPLE = "\u001B[105m";
    private static final String ANSI_BRIGHT_BG_CYAN   = "\u001B[106m";
    private static final String ANSI_BRIGHT_BG_WHITE  = "\u001B[107m";

    private static final String[] BACKGROUNDS = {
            ANSI_BG_BLACK, ANSI_BG_RED, ANSI_BG_GREEN, ANSI_BG_YELLOW,
            ANSI_BG_BLUE, ANSI_BG_PURPLE, ANSI_BG_CYAN, ANSI_BG_WHITE,
            ANSI_BRIGHT_BG_BLACK, ANSI_BRIGHT_BG_RED, ANSI_BRIGHT_BG_GREEN, ANSI_BRIGHT_BG_YELLOW,
            ANSI_BRIGHT_BG_BLUE, ANSI_BRIGHT_BG_PURPLE, ANSI_BRIGHT_BG_CYAN, ANSI_BRIGHT_BG_WHITE
    };

    private final ConcurrentMap<String, Integer> clientForegrounds = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Integer> clientBackgrounds = new ConcurrentHashMap<>();

    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private final AtomicBoolean darkMode = new AtomicBoolean(false);

    public ColorizedPrintStream(OutputStream out) {
        super(out);
    }

    public ColorizedPrintStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public ColorizedPrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
    }

    public ColorizedPrintStream(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public ColorizedPrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public ColorizedPrintStream(File file) throws FileNotFoundException {
        super(file);
    }

    public ColorizedPrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    @Override
    public void println(String x) {
        if(isEnabled() && x != null) {
            int i = x.indexOf(":");
            if(i > 0) {
                String name = x.substring(0, i);
                String message = x.substring(i);
                clientForegrounds.putIfAbsent(name, (int)(Math.random() * FOREGROUNDS.length));
                int foregroundIndex = clientForegrounds.get(name);
                x = String.format(
                        "%s%s%s%s%s",
                        FOREGROUNDS[foregroundIndex],
                        foregroundIndex < 8? ANSI_BRIGHT_BG_WHITE: ANSI_BG_BLACK,
                        name,
                        ANSI_RESET,
                        message
                );
            }
        }
        super.println(x);
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public boolean isDarkMode() {
        return darkMode.get();
    }

    public void setDarkMode(boolean darkMode) {
        if(this.darkMode.getAndSet(darkMode) != darkMode) {
            clientForegrounds.clear();
        }
    }

    public static void main(String[] args) {
        System.out.println("\n  Default text\n");

        for (String fg : FOREGROUNDS) {
            for (String bg : BACKGROUNDS) {
                System.out.print(fg + bg + "  TEST  ");
            }
            System.out.println(ANSI_RESET);
        }

        System.out.println(ANSI_RESET + "\n  Back to default.\n");
    }
}
