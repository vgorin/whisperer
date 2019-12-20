package core911.whisp.client;

import java.math.BigInteger;

/**
 * @author vgorin
 *         file created on 12/20/2019 9:24 AM
 */

class NameUtils {
    private static final String[] NAME_TITLE = {"Big", "Tall", "Small", "Deep", "Slim", "Curvy", "Tiny", "Strong", "Long", "Hard", "Brave", "Funny"};
    private static final String[] NAME_FIRST = {"Juicy", "Tasty", "Salty", "Sweet", "Bitter", "Hot", "Cold", "Dirty", "Fat"};
    private static final String[] NAME_LAST = {"Apricot", "Banana", "Apple", "Carrot", "Potato", "Onion", "Pear", "Octopus", "Ramp", "Cherry", "Basil", "Garlic", "Lime", "Pea", "Mushroom", "Crucian", "Crab", "Catfish", "Aloe", "Cactus"};

    static String generateName(byte[] hash) {
        BigInteger i = new BigInteger(hash).abs();
        String title = NAME_TITLE[i.remainder(BigInteger.valueOf(NAME_TITLE.length)).intValue()];
        String first = NAME_FIRST[i.remainder(BigInteger.valueOf(NAME_FIRST.length)).intValue()];
        String last = NAME_LAST[i.remainder(BigInteger.valueOf(NAME_LAST.length)).intValue()];
        return String.format("%s %s %s", title, first, last);
    }
}
