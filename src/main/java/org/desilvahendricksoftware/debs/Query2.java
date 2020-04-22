package org.desilvahendricksoftware.debs;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.ArrayList;

public class Query2 {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final int windowSize = 1000;

        Requests requests = new Requests(2);
        ArrayList<Point> w2_builder = new ArrayList<>(); //TODO: Determine how to correctly store a list of features.
        EventDetector eventDetector = new EventDetector(0.03, 2, 0.8, 40); /* Using hyper parameters from Python solution for now */

        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.setParallelism(1);

        DataStream<Sample> input = env.addSource(new SourceFunction<Sample>() {
            @Override
            public void run(SourceContext<Sample> sourceContext) throws Exception {
                Sample[] batch;
                while ((batch = requests.get()) != null) {
                    for (Sample sample : batch) {
                        sourceContext.collect(sample);
                    }
                }
            }

            @Override
            public void cancel() {
            }
        });

        //process each record from the json output here and assign watermarks to each record.
        DataStream<Tuple3<Long, Double, Double>> samples = input.map(new MapFunction<Sample, Tuple3<Long, Double, Double>>() {
            @Override
            //f0: id, f1; voltage, f2: current
            public Tuple3<Long, Double, Double> map(Sample sample) throws Exception {
                Long id = sample.i;
                Double voltage = sample.voltage;
                Double current = sample.current;
                Tuple3<Long, Double, Double> ret = new Tuple3<>(id, voltage, current);
                return ret;
            }
        })
                .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<Tuple3<Long, Double, Double>>(Time.milliseconds(20000)) {
                    @Override
                    public long extractTimestamp(Tuple3<Long, Double, Double> element) {
                        return element.f0;
                    }
                });

        DataStream<Tuple3<Long, Double, Double>> features = samples
                .windowAll(SlidingEventTimeWindows.of(Time.milliseconds(windowSize), Time.milliseconds(windowSize))).allowedLateness(Time.milliseconds(20000))
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
                                return;
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
                        if (eventDetector.numWindowsProcessedSinceLastEventDetected > 100 && !eventDetector.eventDetected) {
                            //System.out.println("Emptying the window");
                            w2_builder.clear();
                            eventDetector.numWindowsProcessedSinceLastEventDetected = 0;
                        }
                        w2_builder.add(new Point(x_n.f1, x_n.f2, x_n.f0));
                        Tuple3<Long, Boolean, Integer> ret = eventDetector.predict(x_n.f0, w2_builder.toArray(new Point[w2_builder.size()]));
                        if (ret.f1 == true) {
                            eventDetector.eventDetected = true;
                            eventDetector.numWindowsProcessedSinceLastEventDetected = 0;
                            w2_builder.clear();
                        }
                        requests.post(new Result(ret.f0, ret.f1, ret.f2));
                        System.out.println(ret);
                        out.collect(ret);
                    }
                });

        // execute program
        env.execute("DEBS 2020: Query 2");
        requests.get();
    }
}
