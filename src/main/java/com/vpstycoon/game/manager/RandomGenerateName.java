package com.vpstycoon.game.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomGenerateName {
    private static final String CONSONANTS = "BCDFGHJKLMNPQRSTVWXYZ";
    private static final String VOWELS = "AEIOU";
    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 10;
    private static final int NAME_POOL_SIZE = 100; 

    private static List<String> namePool = new ArrayList<>();

    
    static {
        Random random = new Random();
        for (int i = 0; i < NAME_POOL_SIZE; i++) {
            int length = MIN_LENGTH + random.nextInt(MAX_LENGTH - MIN_LENGTH + 1);
            namePool.add(generateRealisticName(length));
        }
    }

    
    private static String generateRealisticName(int length) {
        Random random = new Random();
        StringBuilder name = new StringBuilder();
        boolean useConsonant = random.nextBoolean(); 

        for (int i = 0; i < length; i++) {
            if (useConsonant) {
                char randomConsonant = CONSONANTS.charAt(random.nextInt(CONSONANTS.length()));
                name.append(randomConsonant);
            } else {
                char randomVowel = VOWELS.charAt(random.nextInt(VOWELS.length()));
                name.append(randomVowel);
            }
            useConsonant = !useConsonant; 
        }

        return name.toString();
    }

    
    public static String generateRandomName() {
        Random random = new Random();
        int index = random.nextInt(namePool.size());
        return namePool.get(index);
    }
}

