package me.wayne.daos.vectors;

import java.io.Serializable;
import java.util.*;

import me.wayne.daos.Pair;
import me.wayne.daos.storevalues.PrintableList;

public class VectorDB implements VectorOperations, Serializable {

    private final Map<String, double[]> database = new HashMap<>();
    private final int dimensions;

    public VectorDB(int dimensions) {
        this.dimensions = dimensions;
    }
    
    public int getDimensions() {
        return dimensions;
    }

    public boolean insert(String id, double[] vector) {
        if (vector.length != dimensions) return false;
        database.put(id, vector);
        return true;
    }

    public double[] get(String id) {
        return database.get(id);
    }

    public List<Pair<String, Double>> findKNearestNeighborsoUsingEuclideanDistance(double[] queryVector, int k) {
        return new PrintableList<>(database.entrySet().stream()
            .map(entry -> new Pair<>(entry.getKey(), VectorOperations.euclideanDistance(queryVector, entry.getValue())))
            .sorted(Comparator.comparingDouble(Pair::getSecond))
            .limit(k)
            .toList());
    }

    public List<Pair<String, Double>> findKNearestNeighborsoUsingCosineSimilarity(double[] queryVector, int k) {
        return new PrintableList<>(database.entrySet().stream()
            .map(entry -> new Pair<>(entry.getKey(), VectorOperations.cosineSimilarity(queryVector, entry.getValue())))
            .sorted(Comparator.comparingDouble(Pair<String, Double>::getSecond).reversed())
            .limit(k)
            .toList());
    }

}
