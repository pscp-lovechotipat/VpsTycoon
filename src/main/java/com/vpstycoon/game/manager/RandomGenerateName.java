package com.vpstycoon.game.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomGenerateName {
    private static final String CONSONANTS = "BCDFGHJKLMNPQRSTVWXYZ";
    private static final String VOWELS = "AEIOU";
    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 10;
    private static final int NAME_POOL_SIZE = 100; // จำนวนชื่อที่สร้างไว้ใน list

    private static List<String> namePool = new ArrayList<>();

    // สร้าง list ของชื่อที่สมจริงไว้ล่วงหน้า
    static {
        Random random = new Random();
        for (int i = 0; i < NAME_POOL_SIZE; i++) {
            int length = MIN_LENGTH + random.nextInt(MAX_LENGTH - MIN_LENGTH + 1);
            namePool.add(generateRealisticName(length));
        }
    }

    // ฟังก์ชันสร้างชื่อที่มีโครงสร้างสมจริง
    private static String generateRealisticName(int length) {
        Random random = new Random();
        StringBuilder name = new StringBuilder();
        boolean useConsonant = random.nextBoolean(); // สุ่มว่าจะเริ่มด้วยพยัญชนะหรือสระ

        for (int i = 0; i < length; i++) {
            if (useConsonant) {
                char randomConsonant = CONSONANTS.charAt(random.nextInt(CONSONANTS.length()));
                name.append(randomConsonant);
            } else {
                char randomVowel = VOWELS.charAt(random.nextInt(VOWELS.length()));
                name.append(randomVowel);
            }
            useConsonant = !useConsonant; // สลับระหว่างพยัญชนะและสระ
        }

        return name.toString();
    }

    // ฟังก์ชันสำหรับดึงชื่อจาก list
    public static String generateRandomName() {
        Random random = new Random();
        int index = random.nextInt(namePool.size());
        return namePool.get(index);
    }
}