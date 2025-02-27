package com.vpstycoon.game.manager;

import java.util.Random;

public class RandomGenerateName {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String generateRandomName(int length) {
        Random random = new Random();
        StringBuilder name = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char randomChar = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
            name.append(randomChar);
        }

        return name.toString();
    }
}
