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

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.io.FileInputFormat;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.io.IOException;
import java.util.ArrayList;

public class Query1 {

	public static void main(String[] args) throws Exception {
		// set up the streaming execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		/*
		 * Have a look at the programming guide for the Java API:
		 * https://flink.apache.org/docs/latest/apis/streaming/index.html
		 *
		 */
		final int windowSize = 1000;

		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

		DataStream<String> input = env.readTextFile(AppBase.pathToData);

		DataStream<Tuple3<Long, Double, Double>> stream = input.map(new MapFunction<String, Tuple3<Long, Double, Double>>() {
			@Override
			//f0: id, f1; voltage, f2: current
			public Tuple3<Long, Double, Double> map(String s) throws Exception {
				String[] currentLine = s.split(",");
				Long id = Long.parseLong(currentLine[0]);
				Double voltage = Double.parseDouble(currentLine[1]);
				Double current = Double.parseDouble(currentLine[2]);
				Tuple3<Long, Double, Double> ret = new Tuple3<>(id,voltage,current);
				return ret;
			}
			})
			.assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Tuple3<Long, Double, Double>>() {
				@Override
				public long extractAscendingTimestamp(Tuple3<Long, Double, Double> element) {
					return element.f0;
				}
			});

		DataStream<Tuple2<double[], double[]>> features = stream
				.windowAll(SlidingEventTimeWindows.of(Time.milliseconds(windowSize), Time.milliseconds(windowSize)))
				.process(new ProcessAllWindowFunction<Tuple3<Long, Double, Double>, Tuple2<double[], double[]>, TimeWindow>() {
					@Override
					public void process(Context context, Iterable<Tuple3<Long, Double, Double>> iterable, Collector<Tuple2<double[], double[]>> collector) throws Exception {
						Double[] voltages = new Double[windowSize];
						Double[] currents = new Double[windowSize];
						int index = 0;
						for (Tuple3<Long,Double, Double> element: iterable) {
							voltages[index] = element.f1;
							currents[index] = element.f2;
							index++;
						}

						//calculate active and reactive power features
						double[] activePower = Utils.calculateActivePower(voltages, currents);
						double[] reactivePower = Utils.calculateReactivePower(voltages, currents);
						Tuple2<double[], double[]> ret = new Tuple2<>(activePower, reactivePower);
						System.out.println(ret);
						collector.collect(ret);
					}
				});

		// execute program
		env.execute("Flink Streaming Java API Skeleton");
	}
}
