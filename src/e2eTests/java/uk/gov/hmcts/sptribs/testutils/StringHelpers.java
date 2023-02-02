package uk.gov.hmcts.sptribs.testutils;

import java.util.Random;

public class StringHelpers {
    
    private StringHelpers() {
    }

    private static final String CHAR_LIST = "abcdefghijklmnopqrstuvwxyz1234567890";

    public static String getRandomString(int length) {
        char firstLetter = getRandomCharacter("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Random random = new Random();
        char[] randomChars = new char[length];
        for (int i = 0; i < length; i++) {
            randomChars[i] = CHAR_LIST.charAt(random.nextInt(CHAR_LIST.length()));
        }
        return new String(randomChars);
    }

    private static char getRandomCharacter(String str) {
        int randomIndex = new Random().nextInt(str.length());
        return str.charAt(randomIndex);
    }

}
