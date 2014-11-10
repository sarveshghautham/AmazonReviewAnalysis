import java.io.IOException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.lib.NullOutputFormat;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ProductIdToName {

	public static class Map extends
			Mapper<LongWritable, Text, NullWritable, NullWritable> {

		private String product_id = "";
		private Cassandra casObj = Cassandra.getInstance();
		
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {

			
			
			String line = value.toString();
			String []tokens = line.split(" ");
			
			for (int i = 0; i < tokens.length; i++) {
				String empty = "";
				if (tokens[i].equals("product/productId:")) {
					product_id = tokens[i+1];
					String query = "INSERT INTO pds_ks.id_to_name (product_id, product_name) "
							+ "VALUES ('"+tokens[i+1]+"', '"+empty+"')";
					System.out.println(query);
					casObj.insertData(query);
				}
				else if (tokens[i].equals("product/title:")) {
					
					String product_name = StringEscapeUtils.escapeSql(ConvertArrayToString(tokens));
					String updateQuery = "UPDATE pds_ks.id_to_name "
							+ "SET product_name ='"+product_name+"' "
									+ "WHERE product_id='"+product_id+"'";
					
					casObj.insertData(updateQuery);
					product_id = "";
				}
			}
		}
		
		public String ConvertArrayToString (String []tokens) {
			
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < tokens.length; i++) {
				sb.append(tokens[i]);
				if (i != tokens.length-1)
					sb.append(" ");
			}
			return sb.toString();
		}
	}

//	public static class Reduce extends
//			Reducer<Text, IntWritable, Text, IntWritable> {
//
//		public void reduce(Text key, Iterable<IntWritable> values,
//				Context context) throws IOException, InterruptedException {
//			int sum = 0;
//			for (IntWritable val : values) {
//				sum += val.get();
//			}
//			context.write(key, new IntWritable(sum));
//		}
//	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();

		Job job = new Job(conf, "ReviewAnalysis");
		job.setJarByClass(WordCount.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(Map.class);
		//job.setReducerClass(Reduce.class);
		job.setNumReduceTasks(0);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputKeyClass(NullOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.waitForCompletion(true);
	}

}