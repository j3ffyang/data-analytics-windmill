package com.ibm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.spark.Accumulator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaFutureAction;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Serializable;
import scala.Tuple2;

public class SparkSort {
	static Logger logger = LoggerFactory.getLogger(SparkSort.class);
	public static final int ANALOG_START_NO = 352;

	public static String GetRealData(String s) {
		if (s.charAt(s.length() - 1) == ';')
			return s.substring(0, s.length() - 1);
		return s;
	}

	public static Date getTimeFromText(String time) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.parse(time);
	}

	public static void main6(String[] args) throws Exception {
		int argslen = args.length;
		String inputPath = args[0];
		String outputPath = args[1];

		int iAnalogSortNo = 1;
		if (argslen > 2)
			iAnalogSortNo = Integer.parseInt(args[2]);
		iAnalogSortNo = ANALOG_START_NO + iAnalogSortNo - 1;

		int iOutputInt = 100;
		if (argslen > 3)
			iOutputInt = Integer.parseInt(args[3]);

		int iOutputStartNo = 1;
		if (argslen > 4)
			iOutputStartNo = Integer.parseInt(args[4]);

		String startTime = "";
		if (argslen > 5)
			startTime = args[5];

		String endTime = "";
		if (argslen > 6)
			endTime = args[6];

		Date iStartTime = null;
		try {
			iStartTime = getTimeFromText(startTime);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

		Date iEndTime = null;
		try {
			iEndTime = getTimeFromText(endTime);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

		SparkConf sparkConf = new SparkConf().setAppName("SparkSort");
		JavaSparkContext context = new JavaSparkContext(sparkConf);
		Job job = Job.getInstance();
		// job.setJarByClass(SparkSort.class);

		final Accumulator<Integer> aSortNo = context.accumulator(0);
		final Broadcast<Integer> aAnalogSortNo = context
				.broadcast(iAnalogSortNo);
		final Broadcast<Integer> aOutputInt = context.broadcast(iOutputInt);
		final Broadcast<Integer> aOutputStartNo = context
				.broadcast(iOutputStartNo);
		final Broadcast<Date> aStartTime = context.broadcast(iStartTime);
		final Broadcast<Date> aEndTime = context.broadcast(iEndTime);

		JavaRDD<String> datas = context.textFile(inputPath);

		JavaRDD<String> results = datas;
		if (iStartTime!=null)
			results = datas.filter(new Function<String, Boolean>() {
				public Boolean call(String line) {
					String[] stringArray = line.split(",");

					Date time;
					try {
						time = getTimeFromText(stringArray[0]);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}
					if (time.getTime() < aStartTime.getValue().getTime()
							|| time.getTime() >= aEndTime.getValue().getTime())
						return false;
					return true;
				}
			});

		results = results.sortBy(new Function<String, Float>() {
			public Float call(String s) {
				s = GetRealData(s);
				String[] stringArray = s.split(",");
				String sFloat = stringArray[aAnalogSortNo.value()];

				Float iFloat = 0f;
				try {
					iFloat = 0 - Float.parseFloat(sFloat);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return iFloat;
			}
		}, true, 1).filter(new Function<String, Boolean>() {
			public Boolean call(String s) {
				aSortNo.add(1);
				return aSortNo.localValue() % aOutputInt.value() == aOutputStartNo
						.value();
			}
		});

		long iNo = 0;
		for (String x : results.collect()) {
			iNo++;
			System.out.println(iNo + "\t" + x);
		}

		context.stop();
		context.close();
	}

	public static void main(String[] args) throws Exception {
		main6(args);
	}

}
