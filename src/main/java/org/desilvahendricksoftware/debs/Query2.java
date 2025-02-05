package org.desilvahendricksoftware.debs;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Query2 {
    public static double sum(Double[] array) {
        int sum = 0;
        for (double value : array) {
            sum += value;
        }
        return sum;
    }
    public static double avg(Double[] array) {
        double sum = sum(array);
        return sum / array.length;
    }

    public static void run() throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final int windowSize = 1000;
        final int MAX_TIMEOUT = 25200000; //Max timeout of 7 hours until give up to make the first connection
        final int WAIT_TIME = 5000; //Wait 5 seconds between requests when attempting to make the first request

        Requests requests = new Requests(2);
        ArrayList<Point> w2_builder = new ArrayList<>(); //TODO: Determine how to correctly store a list of features.
        EventDetector eventDetector = new EventDetector(0.03, 2, 0.8, 40); /* Using hyper parameters from Python solution for now */

        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.setParallelism(1);

        DataStream<Tuple3<Long, Double, Double>> input =  env.addSource(new SourceFunction<Tuple3<Long, Double, Double>>() {
            @Override
            public void run(SourceContext<Tuple3<Long, Double, Double>> sourceContext) throws Exception {
                Sample[] batch;
                boolean serverIsReady = false;
                int timeSpentWaiting = 0;
                while (!serverIsReady) {
                    if (timeSpentWaiting >= MAX_TIMEOUT) {
                        System.out.println("Timed out");
                        return;
                    }
                    try {
                        //Try to get the first batch
                        batch = requests.getSample();
                        for (Sample sample: batch) {
                            sourceContext.collect(new Tuple3(sample.i, sample.voltage, sample.current));
                        }
                        serverIsReady = true;
                        System.out.println("Server is ready for Query 2. Collecting samples...");
                    } catch (IOException e) {
                        System.out.println("Server still not ready. Waiting " + WAIT_TIME / 1000 + " seconds to try again.");
                        Thread.sleep(WAIT_TIME);
                        timeSpentWaiting = timeSpentWaiting + WAIT_TIME;
                    }
                }
                while ((batch = requests.getSample()) != null) {
                    for (Sample sample: batch) {
                        sourceContext.collect(new Tuple3(sample.i, sample.voltage, sample.current));
                    }
                }
            }
            @Override
            public void cancel() {}
        })
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Tuple3<Long, Double, Double>>() {
                    @Override
                    public long extractAscendingTimestamp(Tuple3<Long, Double, Double> element) {
                        return element.f0;
                    }
                });

        DataStream<Tuple3<Long, Double, Double>> features = input
                .windowAll(SlidingEventTimeWindows.of(Time.milliseconds(windowSize), Time.milliseconds(windowSize)))
                .process(new ProcessAllWindowFunction<Tuple3<Long, Double, Double>, Tuple3<Long, Double, Double>, TimeWindow>() {
                    @Override
                    public void process(Context context, Iterable<Tuple3<Long, Double, Double>> iterable, Collector<Tuple3<Long, Double, Double>> collector) throws Exception {
                        Double[] voltages = new Double[windowSize];
                        Double[] currents = new Double[windowSize];
                        int index = 0;
                        for (Tuple3<Long, Double, Double> element : iterable) {
                            voltages[index] = element.f1;
                            currents[index] = element.f2;
                            index++;
                        }
                        for (int i = 0; i < windowSize; i++) {
                            if (voltages[i] == null || currents[i] == null) {
//                                System.out.println("here");
                                Double[] voltageRange = Arrays.copyOfRange(voltages, 0, i);
                                Double[] currentsRange = Arrays.copyOfRange(voltages, 0, i);
                                voltages[i] = avg(voltageRange) + Math.random() * 0.03;
                                currents[i] = avg(currentsRange) + Math.random() * 0.03;


                            }

                        }

                        //calculate active and reactive power features
                        double activePower = Math.log(Utils.calculateActivePower(voltages, currents));
                        double reactivePower = Math.log(Utils.calculateReactivePower(voltages, currents));
                        Tuple3<Long, Double, Double> ret = new Tuple3<>(context.window().getStart() / windowSize, activePower, reactivePower);
                        collector.collect(ret);
                    }
                });


        //now we need to feed these features into a window of increasing size. On that window,apply the predict function
        DataStream<Tuple3<Long, Boolean, Integer>> events = features
                .process(new ProcessFunction<Tuple3<Long, Double, Double>, Tuple3<Long, Boolean, Integer>>() {
                    @Override
                    public void processElement(Tuple3<Long, Double, Double> x_n, Context context, Collector<Tuple3<Long, Boolean, Integer>> out) throws Exception {
                        eventDetector.numWindowsProcessedSinceLastEventDetected++;
                        //If an event is not detected and w2 has more than 100 elements, empty the window
                        if (eventDetector.numWindowsProcessedSinceLastEventDetected > 100) {
                            w2_builder.clear();
                            eventDetector.numWindowsProcessedSinceLastEventDetected = 0;
                        }
                        w2_builder.add(new Point(x_n.f1, x_n.f2, x_n.f0));
                        Tuple3<Long, Boolean, Integer> ret = eventDetector.predict(x_n.f0, w2_builder.toArray(new Point[w2_builder.size()]));
                        if (ret.f1 == true) {
                            eventDetector.numWindowsProcessedSinceLastEventDetected = 0;
                            w2_builder.clear();
                        }

                        requests.post(new Result(ret.f0, ret.f1, ret.f2));
                        out.collect(ret);
                    }
                });

        env.execute("DEBS 2020: Query 2");
        System.out.println("Query 2 complete.");
        System.out.println(requests.get(requests.endpoint));
    }

    public static void main(String[] args) throws Exception {
        Query2.run();
    }
}