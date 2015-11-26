package com.ibm;

//http://www.aboutyun.com/thread-7046-1-1.html
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapReduceSort implements Constants {
	static Logger logger = LoggerFactory.getLogger(MapReduceSort.class);

	public static Date getTimeFromText(String time) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.parse(time);
	}

	public static class Map extends Mapper<Object, Text, FloatWritable, Text> {

		private static FloatWritable data = new FloatWritable();

		int iOutputInt;
		int iAnalogSortNo;
		int iOutputStartNo;
		String startTime;
		String endTime;
		Date iStartTime = null;
		Date iEndTime = null;

		@Override
		protected void setup(
				Mapper<Object, Text, FloatWritable, Text>.Context context)
				throws IOException, InterruptedException {
			super.setup(context);
			Configuration cfg = context.getConfiguration();

			iOutputInt = Integer.parseInt(cfg.get(OUTPUT_INTERVAL_INT_NAME));
			iAnalogSortNo = ANALOG_START_NO
					+ Integer.parseInt(cfg.get(ANALOG_SORT_NAME)) - 1;
			iOutputStartNo = Integer.parseInt(cfg.get(OUTPUT_START_NO_NAME));

			startTime = cfg.get(START_TIME_NAME);
			endTime = cfg.get(END_TIME_NAME);

			if (startTime!=null) {
				try {
					iStartTime = getTimeFromText(startTime);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (endTime!=null) {
				try {
					iEndTime = getTimeFromText(endTime);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// 实现map函数
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString().trim();
			// 去掉最后的;号
			line = line.substring(0, line.length() - 1);
			try {
				String[] stringArray = line.split(",");

				if (iStartTime != null) {
					Date time = getTimeFromText(stringArray[0]);
					if (time.getTime() < iStartTime.getTime()
							|| time.getTime() >= iEndTime.getTime())
						return;
				}
				String sFloat = stringArray[iAnalogSortNo];
				data.set(0 - Float.parseFloat(sFloat));
				context.write(data, new Text(line));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("map failed, line=" + line);
				e.printStackTrace();
			}
		}
	}

	// reduce将输入中的key复制到输出数据的key上，
	// 然后根据输入的value-list中元素的个数决定key的输出次数
	// 用全局linenum来代表key的位次
	public static class Reduce extends Reducer<FloatWritable, Text, Text, Text> {
		private static IntWritable linenum = new IntWritable(1);

		int iOutputInt;
		int iAnalogSortNo;
		int iOutputStartNo;

		@Override
		protected void setup(
				Reducer<FloatWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			super.setup(context);
			Configuration cfg = context.getConfiguration();

			iOutputInt = Integer.parseInt(cfg.get(OUTPUT_INTERVAL_INT_NAME));
			iAnalogSortNo = ANALOG_START_NO
					+ Integer.parseInt(cfg.get(ANALOG_SORT_NAME)) - 1;
			iOutputStartNo = Integer.parseInt(cfg.get(OUTPUT_START_NO_NAME));
		}

		// 实现reduce函数
		public void reduce(FloatWritable key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			for (Text val : values) {
				if (linenum.get() % iOutputInt == iOutputStartNo)
					System.out.println(linenum + "\t" + val.toString());
				linenum = new IntWritable(linenum.get() + 1);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		int argslen = args.length;
		String inputPath = args[0];
		String outputPath = args[1];

		String analogSortNo = "2";
		if (argslen > 2)
			analogSortNo = args[2];

		String outputIntervalInt = "10000000";
		if (argslen > 3)
			outputIntervalInt = args[3];

		String outputStartNo = "1";
		if (argslen > 4)
			outputStartNo = args[4];

		String startTime = "";
		if (argslen > 5)
			startTime = args[5];

		String endTime = "";
		if (argslen > 6)
			endTime = args[6];

		Configuration conf = new Configuration();
		conf.set(OUTPUT_INTERVAL_INT_NAME, outputIntervalInt);
		conf.set(ANALOG_SORT_NAME, analogSortNo);
		conf.set(OUTPUT_START_NO_NAME, outputStartNo);
		conf.set(START_TIME_NAME, startTime);
		conf.set(END_TIME_NAME, endTime);

		Job job = new Job(conf, "MapReduce Sort");
		job.setJarByClass(MapReduceSort.class);

		// 设置Map和Reduce处理类
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		// 设置输出类型
		job.setOutputKeyClass(FloatWritable.class);
		job.setOutputValueClass(Text.class);

		// 设置输入和输出目录
		FileInputFormat.addInputPath(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPath));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
