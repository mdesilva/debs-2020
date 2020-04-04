package org.desilvahendricksoftware.debs;

import org.apache.flink.api.java.tuple.Tuple2;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import static org.junit.Assert.*;

public class DBSCANTest {

    public Tuple2<Double,Double>[] readSamplesFromCsv(String filepath) {
        ArrayList<Tuple2<Double, Double>> samplesBuilder = new ArrayList();
        try {
            File input = new File(filepath);
            Scanner reader = new Scanner(input);
            while (reader.hasNextLine()) {
                String[] valuesAsStrings = reader.nextLine().split(",");
                Tuple2<Double, Double> values = new Tuple2(Double.parseDouble(valuesAsStrings[0]), Double.parseDouble(valuesAsStrings[1]));
                samplesBuilder.add(values);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return samplesBuilder.toArray(new Tuple2[samplesBuilder.size()]);
    }

    @Test
    public void DBSCANShouldReturn4Clusters() {
        DBSCAN dbscan = new DBSCAN(0.3, 10);
        dbscan.fit(readSamplesFromCsv("dataset/4TrueClusters.csv"));
        assertEquals("With an epsilon of 0.3 and minimum # of points of 10, DBSCAN should return 4 clusters for a dataset containing 4 true clusters.",
                4,
                dbscan.numberOfClustersWithoutNoiseCluster);
    }

    @Test
    public void DBSCANShouldReturn5Clusters() {
        DBSCAN dbscan = new DBSCAN(0.20, 20);
        dbscan.fit(readSamplesFromCsv("dataset/5TrueClusters.csv"));
        assertEquals("With an epsilon of 0.20 and a minimum # of points of 20, DBSCAN should return 5 clusters for a dataset containing 5 true clusters.",
                5,
                dbscan.numberOfClustersWithoutNoiseCluster);
    }
}
