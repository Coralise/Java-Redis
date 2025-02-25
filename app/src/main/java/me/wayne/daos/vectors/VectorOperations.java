package me.wayne.daos.vectors;

import java.util.List;

import me.wayne.daos.Pair;

public interface VectorOperations {
    boolean insert(String id, double[] vector);
    double[] get(String id);
    List<Pair<String, Double>> findKNearestNeighborsoUsingEuclideanDistance(double[] queryVector, int k);
    List<Pair<String, Double>> findKNearestNeighborsoUsingCosineSimilarity(double[] queryVector, int k);

    public static double euclideanDistance(double[] v1, double[] v2) {
        double sum = 0.0;
        for (int i = 0; i < v1.length; i++) {
            sum += Math.pow(v1[i] - v2[i], 2);
        }
        return Math.sqrt(sum);
    }

    public static double cosineSimilarity(double[] v1, double[] v2) {
        double dotProduct = 0.0;
        double  normA = 0.0;
        double  normB = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i]; // Compute dot product
            normA += Math.pow(v1[i], 2); // Compute norm of A
            normB += Math.pow(v2[i], 2); // Compute norm of B
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public static Double[] addVectors(double[] v1, double[] v2) {
        Double[] result = new Double[v1.length];
        for (int i = 0; i < v1.length; i++) {
            result[i] = v1[i] + v2[i];
        }
        return result;
    }

    public static Double[] subtractVectors(double[] v1, double[] v2) {
        Double[] result = new Double[v1.length];
        for (int i = 0; i < v1.length; i++) {
            result[i] = v1[i] - v2[i];
        }
        return result;
    }

    public static Double dotProduct(double[] v1, double[] v2) {
        double result = 0.0;
        for (int i = 0; i < v1.length; i++) {
            result += v1[i] * v2[i];
        }
        return result;
    }
}
