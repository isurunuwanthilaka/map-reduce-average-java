package com.isuru;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

public class Average {
    public static class PrimaryMap extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, IntWritable> {

        public void map(LongWritable key, Text value, OutputCollector<IntWritable, IntWritable> output, Reporter reporter) throws IOException {
            int max = 4;
            int min = 1;
            Random r = new Random();
            int t = r.nextInt(max) + min;
            IntWritable bucketNo = new IntWritable(t);
            IntWritable number = new IntWritable(Integer.valueOf(value.toString()));
            output.collect(bucketNo, number);
        }
    }

    public static class PrimaryReduce extends MapReduceBase implements Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
        public void reduce(IntWritable key, Iterator<IntWritable> values, OutputCollector<IntWritable, IntWritable> output, Reporter reporter) throws IOException {
            int sum = 0;
            int count = 0;
            while (values.hasNext()) {
                sum += values.next().get();
                count = count + 1;
            }
            output.collect(new IntWritable(count), new IntWritable(sum));
        }
    }

    public static class SecondaryMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

        private Text one = new Text("One");

        public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
            output.collect(one, value);
        }
    }

    public static class SecondaryReduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
            Integer sum = 0;
            Integer count = 0;
            while (values.hasNext()) {
                String value = values.next().toString();
                String[] temp = value.split("\\s+");
                if (temp.length == 2) {
                    sum += Integer.valueOf(temp[1]);
                    count += Integer.valueOf(temp[0]);
                }
            }

            String avg = String.valueOf(sum.doubleValue() / count);
            output.collect(new Text("output"), new Text(avg));
        }
    }


    public static void main(String[] args) throws Exception {
        JobConf conf1 = new JobConf(Average.class);
        conf1.setJobName("Job 1");
        conf1.setOutputKeyClass(IntWritable.class);
        conf1.setOutputValueClass(IntWritable.class);
        conf1.setMapperClass(PrimaryMap.class);
        conf1.setReducerClass(PrimaryReduce.class);
        conf1.setInputFormat(TextInputFormat.class);
        conf1.setOutputFormat(TextOutputFormat.class);
        FileInputFormat.setInputPaths(conf1, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf1, new Path(args[1]));
        JobClient.runJob(conf1).waitForCompletion();

        JobConf conf2 = new JobConf(Average.class);
        conf2.setJobName("Job 2");
        conf2.setOutputKeyClass(Text.class);
        conf2.setOutputValueClass(Text.class);
        conf2.setMapperClass(SecondaryMap.class);
        conf2.setReducerClass(SecondaryReduce.class);
        conf2.setInputFormat(TextInputFormat.class);
        conf2.setOutputFormat(TextOutputFormat.class);
        FileInputFormat.setInputPaths(conf2, new Path(args[1]));
        FileOutputFormat.setOutputPath(conf2, new Path(args[2]));
        JobClient.runJob(conf2);
    }
}
