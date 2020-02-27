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
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.io.IOException;

public class Query1 {

	public static void main(String[] args) throws Exception {
		// set up the streaming execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		/*
		 * Have a look at the programming guide for the Java API:
		 * https://flink.apache.org/docs/latest/apis/streaming/index.html
		 *
		 */

		DataStream<String> input = env.readTextFile("/Users/manujadesilva/Desktop/CS591DSPA/debs/dataset/in1.csv");

		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

		DataStream<Tuple3<Long, Float, Float>> parsed = input.map(new MapFunction<String, Tuple3<Long, Float, Float>>() {
			@Override
			public Tuple3<Long, Float, Float> map(String s) throws Exception {
				String[] currentLine = s.split(",");
				Long id = Long.parseLong(currentLine[0]);
				Float voltage = Float.parseFloat(currentLine[1]);
				Float current = Float.parseFloat(currentLine[2]);
				Tuple3<Long, Float, Float> ret = new Tuple3<>(id,voltage,current);
				System.out.println(ret);
				return ret;
			}
		});

		// execute program
		env.execute("Flink Streaming Java API Skeleton");
	}
}
