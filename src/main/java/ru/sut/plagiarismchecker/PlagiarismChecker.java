package ru.sut.plagiarismchecker;

import java.util.*;

/**
 * Класс для проверки плагиата с использованием метода МинХэш (MinHash).
 * Предоставляет методы для генерации подписи текста, расчета схожести и оценки оригинальности.
 */
public class PlagiarismChecker {

    private static final int CHUNK_SIZE = 500;
    private static final int HASH_FUNCTIONS = 100;
    private static final int SHINGLE_SIZE = 10;

    /**
     * Генерирует МинХэш-подпись для одного текстового блока (чанка).
     *
     * @param text текстовый блок для которого создается подпись
     * @return массив значений МинХэш-подписи
     */
    public static int[] generateOneChunkSignature(String text) {
        Set<String> shingles = getShingles(text);
        int[] minHashSignature = new int[HASH_FUNCTIONS];
        Arrays.fill(minHashSignature, Integer.MAX_VALUE);

        for (String shingle : shingles) {
            for (int i = 0; i < HASH_FUNCTIONS; i++) {
                int hash = getHash(shingle, i);
                if (hash < minHashSignature[i]) {
                    minHashSignature[i] = hash;
                }
            }
        }

        return minHashSignature;
    }

    /**
     * Извлекает шинглы из текста.
     * Шингл - это подстрока фиксированной длины (SHINGLE_SIZE).
     *
     * @param text текст, из которого извлекаются шинглы
     * @return множество шинглов
     */
    public static Set<String> getShingles(String text) {
        Set<String> shingles = new HashSet<>();
        for (int i = 0; i <= text.length() - SHINGLE_SIZE; i++) {
            shingles.add(text.substring(i, i + SHINGLE_SIZE));
        }
        return shingles;
    }

    /**
     * Возвращает хеш-код для данного шингла с использованием заданного семени.
     *
     * @param shingle шингл для хеширования
     * @param seed семя для хеширования
     * @return хеш-код шингла
     */
    public static int getHash(String shingle, int seed) {
        return shingle.hashCode() ^ (seed * 0x9e3779b9);
    }

    /**
     * Разбивает текст на блоки фиксированной длины (CHUNK_SIZE).
     *
     * @param text текст для разбивки
     * @return список текстовых блоков
     */
    public static List<String> textSplit(String text) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start + CHUNK_SIZE <= text.length()) {
            chunks.add(text.substring(start, start + CHUNK_SIZE));
            start += CHUNK_SIZE;
        }
        return chunks;
    }

    /**
     * Генерирует МинХэш-подпись для всего текста, разбивая его на блоки.
     *
     * @param text текст для которого создается подпись
     * @return список МинХэш-подписей для каждого блока
     */
    public static List<int[]> getMinHashSignature(String text) {
        List<String> textChunks = textSplit(text);
        List<int[]> signature = new ArrayList<>();
        for (String chunk : textChunks) {
            int[] chunkSignature = generateOneChunkSignature(chunk);
            signature.add(chunkSignature);
        }
        return signature;
    }

    /**
     * Рассчитывает схожесть двух текстов на основе их МинХэш-подписей.
     *
     * @param signature1 МинХэш-подпись первого текста
     * @param signature2 МинХэш-подпись второго текста
     * @return степень схожести (от 0 до 1)
     */
    public static double getSimilarity(List<int[]> signature1, List<int[]> signature2) {

        double totalSum = 0;

        for (int i = 0; i < signature1.size(); i++) {
            int coincidencesSum = 0;
            for (int j = 0; j < signature2.size(); j++) {
                int coincidences = 0;
                for(int k = 0; k < signature1.get(i).length; k++) {
                    if(signature1.get(i)[k] == signature2.get(j)[k]) {
                        coincidences++;
                    }
                }
                coincidencesSum += coincidences;
            }
            totalSum += Math.min(1, (double) coincidencesSum / HASH_FUNCTIONS);
        }
        return totalSum / signature1.size();
    }

    /**
     * Вычисляет процент оригинальности текста на основе схожести его МинХэш-подписи с другой подписью.
     *
     * @param signature1 МинХэш-подпись первого текста
     * @param signature2 МинХэш-подпись второго текста
     * @return процент оригинальности (от 0 до 100)
     */
    public static int getOriginalityPercent(List<int[]> signature1, List<int[]> signature2) {
        double similarity = getSimilarity(signature1, signature2);
        return (int) ((1 - similarity) * 100);
    }

    /**
     * Вычисляет процент оригинальности текста на основе заданной степени схожести.
     *
     * @param similarity степень схожести (от 0 до 1)
     * @return процент оригинальности (от 0 до 100)
     */
    public static int getOriginalityPercent(double similarity) {
        return (int) ((1 - similarity) * 100);
    }

    /**
     * Сериализует МинХэш-подпись в строку.
     *
     * @param signature список МинХэш-подписей для каждого блока
     * @return строка, представляющая сериализованную подпись
     */
    public static String serializeSignature(List<int[]> signature) {
        StringBuilder builder = new StringBuilder();
        for (int[] chunkSignature : signature) {
            for (int hash : chunkSignature) {
                builder.append(hash).append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(";");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    /**
     * Десериализует строку в МинХэш-подпись.
     *
     * @param serializedSignature строка, представляющая сериализованную подпись
     * @return список МинХэш-подписей для каждого блока
     */
    public static List<int[]> deserializeSignature(String serializedSignature) {
        List<int[]> signature = new ArrayList<>();
        String[] chunks = serializedSignature.split(";");
        for (String chunk : chunks) {
            String[] hashes = chunk.split(",");
            int[] chunkSignature = new int[HASH_FUNCTIONS];
            for (int i = 0; i < HASH_FUNCTIONS; i++) {
                chunkSignature[i] = Integer.parseInt(hashes[i]);
            }
            signature.add(chunkSignature);
        }
        return signature;
    }

}
