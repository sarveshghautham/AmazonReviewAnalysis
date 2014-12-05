import java.io.IOException;
import java.util.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class POSTagMR {

	public static class Map extends
			Mapper<LongWritable, Text, Text, IntWritable> {
		private final static IntWritable one = new IntWritable(1);
		
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			String line = value.toString();
			String []tokens = line.split(" ");
			
			String productId = tokens[0];
			String productName = ExtractProductName(tokens);
			String reviews = ConvertArrayToString(tokens);
			ArrayList<String> adjectives = POSTagger.POSTaggedWords(reviews);
			
			for (String word : adjectives) {
				String newKey = productId + " " + productName + " " + word;		
				context.write(new Text(newKey), one);
			}
		}
		
		public String ExtractProductName (String []tokens) {
			StringBuilder sb = new StringBuilder();
			int i = 1;
			while(!tokens[i].equals("||\t")) {
				sb.append(tokens[i]);
				sb.append(" ");
				i++;
			}
			return sb.toString();
		}
		
		public String ConvertArrayToString (String []tokens) {
			int i = 1;
			while(!tokens[i].equals("||\t")) {
				i++;
			}
			i++;
			StringBuilder sb = new StringBuilder();
			for (int j = i; j < tokens.length; j++) {
				sb.append(tokens[j]);
				if (j != tokens.length-1)
					sb.append(" ");
			}
			return sb.toString();
		}
	}

	public static class Reduce extends
			Reducer<Text, IntWritable, Text, IntWritable> {

		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			context.write(key, new IntWritable(sum));
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();

		Job job = new Job(conf, "POSTagMR");
		job.setJarByClass(POSTagMR.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.waitForCompletion(true);
	}

}