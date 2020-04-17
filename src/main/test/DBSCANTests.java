public class DBSCANTests {
//
//    private DBSCAN testScan = new DBSCAN(0.5, 3);
//
//    // cluster 0
//    private final Point p1 = new Point(1.1, 1.23);
//    private final Point p2 = new Point(1.34, 1.54);
//    private final Point p3 = new Point(1.01, 1.13);
//    private final Point p4 = new Point(1.03, 1.24);
//    private final Point p5 = new Point(1.56, 1.89);
//
//    // cluster 1
//    private final Point p6 = new Point(4.0, 7.0);
//    private final Point p7 = new Point(4.02, 7.08);
//    private final Point p8 = new Point(4.03, 7.02);
//    private final Point p9 = new Point(4.04, 7.04);
//
//    // noise
//    private final Point p10 = new Point(23, 19);
//
//    @Test
//    public void testCalculateDistance() throws Exception {
//
//        assertEquals(5.0, DBSCAN.calculateDistance(new Point(1.0, 3.0), new Point(4.0, 7.0)), 0.0);
//        assertEquals(118.6, DBSCAN.calculateDistance(new Point(23.0, -43.0), new Point(94.0,-138.0)), 0.001);
//        assertEquals(0.0, DBSCAN.calculateDistance(new Point(0.0, 0.0), new Point(0.0, 0.0)), 0.0);
//        assertEquals(34.195, DBSCAN.calculateDistance(new Point(9.23,0.35), new Point(42.345,8.88)), 0.001);
//
//        assertEquals(0.392, DBSCAN.calculateDistance(p1, p2), 0.001);
//        assertEquals(0.1118, DBSCAN.calculateDistance(p3, p4), 0.001);
//        assertEquals(5.662, DBSCAN.calculateDistance(p5, p6), 0.001);
//
//        assertEquals(true, DBSCAN.calculateDistance(p5, p6) > testScan.getEpsilon());
//
//        // p1's neighbors
//        assertEquals(true, DBSCAN.calculateDistance(p1, p2) < testScan.getEpsilon());
//        assertEquals(true, DBSCAN.calculateDistance(p1, p3) < testScan.getEpsilon());
//        assertEquals(true, DBSCAN.calculateDistance(p1, p4) < testScan.getEpsilon());
//        // non-neighbors
//        assertEquals(true, DBSCAN.calculateDistance(p1, p5) > testScan.getEpsilon());
//        assertEquals(true, DBSCAN.calculateDistance(p1, p6) > testScan.getEpsilon());
//    }
//
//    @Test
//    public void testGetNeighbors() throws Exception {
//         Point [] points = new Point[] {p1, p2, p3, p4, p5, p6};
//
//        assertEquals(true, testScan.getNeighbors(p1, points).contains(p2));
//        assertEquals(true, testScan.getNeighbors(p1, points).contains(p3));
//        assertEquals(true, testScan.getNeighbors(p1, points).contains(p4));
//
//        assertEquals(false, testScan.getNeighbors(p1, points).contains(p1));
//        assertEquals(false, testScan.getNeighbors(p1, points).contains(p5));
//        assertEquals(false, testScan.getNeighbors(p1, points).contains(p6));
//
//    }
//
//    @Test
//    public void testFit() throws Exception {  Point [] points = new Point[] {p1, p10, p8, p3, p4, p5, p6, p7, p2, p9};
//
//        testScan.fit(points);
//
//        assertEquals(0, testScan.clusters[0]);
//        assertEquals(-1, testScan.clusters[1]);
//        assertEquals(1, testScan.clusters[2]);
//        assertEquals(0, testScan.clusters[3]);
//        assertEquals(0, testScan.clusters[4]);
//        assertEquals(0, testScan.clusters[5]);
//        assertEquals(1, testScan.clusters[6]);
//        assertEquals(1, testScan.clusters[7]);
//        assertEquals(0, testScan.clusters[8]);
//        assertEquals(1, testScan.clusters[9]);
//
//    }

//    @Test
//    public void DBSCANShouldReturn4Clusters() {
//        DBSCAN dbscan = new DBSCAN(0.165, 2);
//        dbscan.fit(readSamplesFromCsv("dataset/4TrueClusters.csv"));
//        assertEquals("With an epsilon of 0.165 and minimum # of points of 2, DBSCAN should return 4 clusters for a dataset containing 4 true clusters.",
//                4,
//                dbscan.numberOfClustersWithoutNoiseCluster);
//    }
//
//    @Test
//    public void DBSCANShouldReturn5Clusters() {
//        DBSCAN dbscan = new DBSCAN(0.15, 2);
//        dbscan.fit(readSamplesFromCsv("dataset/5TrueClusters.csv"));
//        assertEquals("With an epsilon of 0.20 and a minimum # of points of 20, DBSCAN should return 5 clusters for a dataset containing 5 true clusters.",
//                5,
//                dbscan.numberOfClustersWithoutNoiseCluster);
//    }
//
//    @Test
//    public void DBSCANMergeRightToLeftCollectionTest(){
//        DBSCAN dbscan = new DBSCAN(0.20, 20);
//        ArrayList<Point> input1 = new ArrayList<>();
//        ArrayList<Point> input2 = new ArrayList<>();
//        ArrayList<Point> expected = new ArrayList<>();
//        input1.add(new Point(1.0,2.0));
//        input1.add(new Point(1.1,2.1));
//        input1.add(new Point(1.2,2.2));
//        input1.add(new Point(1.3,2.3));
//        input2.add(new Point(2.0,3.0));
//        input2.add(new Point(2.1,3.1));
//        input2.add(new Point(1.2,2.2));
//        input2.add(new Point(1.3,2.3));
//        expected.add(new Point(1.0,2.0));
//        expected.add(new Point(1.1,2.1));
//        expected.add(new Point(1.2,2.2));
//        expected.add(new Point(1.3,2.3));
//        expected.add(new Point(2.0,3.0));
//        expected.add(new Point(2.1,3.1));
//        assertArrayEquals(expected.toArray(), dbscan.mergeRightToLeftCollection(input1,input2).toArray());
//    }
//
//    public Point[] readSamplesFromCsv(String filepath) {
//        ArrayList<Tuple3<Double, Double, Long>> samplesBuilder = new ArrayList();
//        try {
//            File input = new File(filepath);
//            Scanner reader = new Scanner(input);
//            while (reader.hasNextLine()) {
//                String[] valuesAsStrings = reader.nextLine().split(",");
//                Point values = new Point(Double.parseDouble(valuesAsStrings[0]), Double.parseDouble(valuesAsStrings[1]), 0L);
//                samplesBuilder.add(values);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        return samplesBuilder.toArray(new Point[samplesBuilder.size()]);
//    }
}