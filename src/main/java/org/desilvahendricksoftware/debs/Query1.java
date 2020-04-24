/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

public class Query1 {


	public static void run() throws Exception, Requests.InvalidQueryException {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		final int windowSize = 1000;
		final int MAX_TIMEOUT = 900000; //Max timeout of 15 minutes, or 900000ms until give up to make the first connection
		final int WAIT_TIME = 5000; //Wait 5 seconds between requests when attempting to make the first request
		Requests requests = new Requests(1);
		ArrayList<Point> w2_builder = new ArrayList<>(); //TODO: Determine how to correctly store a list of features.
		EventDetector eventDetector = new EventDetector(0.03, 2, 0.8, 40); /* Using hyper parameters from Python solution for now */

		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
		env.setParallelism(1);
		env.getConfig().setAutoWatermarkInterval(5);

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
						batch = requests.get();
						for (Sample sample: batch) {
							sourceContext.collect(new Tuple3(sample.i, sample.voltage, sample.current));
						}
						serverIsReady = true;
						System.out.println("Server is ready for Query 1. Collecting samples...");
					} catch (IOException e) {
						System.out.println("Server still not ready. Waiting " + WAIT_TIME / 1000 + " seconds to try again.");
						Thread.sleep(WAIT_TIME);
						timeSpentWaiting = timeSpentWaiting + WAIT_TIME;
					}
				}
				while ((batch = requests.get()) != null) {
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
						for (Tuple3<Long,Double, Double> element: iterable) {
							voltages[index] = element.f1;
							currents[index] = element.f2;
							index++;
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
				.process(new ProcessFunction<Tuple3<Long,Double,Double>, Tuple3<Long, Boolean, Integer>>() {
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
						//System.out.println(ret);
						out.collect(ret);
					}
				});

		// execute program
		env.execute("DEBS 2020: Query 1");
		System.out.println("Query 1 complete.");
		requests.get();
	}

	public static void main(String[] args) throws Exception {
		Query1.run();
	}
}
