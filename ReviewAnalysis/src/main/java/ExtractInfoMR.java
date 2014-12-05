import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;

import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class ExtractInfoMR {

	public static class Map extends
			Mapper<LongWritable, Text, Text, Text> {

		private String product_id = "";
		private String prev_product_id = "";
		private String product_name = "";
		private String prev_product_name = "";
		private String product_review_summary = "";
		private String product_review_text = "";
		private String temp_product_reviews="";
		private boolean first = true;

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {

			String line = value.toString();
			String []tokens = line.split(":");
			
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].equals("product/productId")) {
					product_id = tokens[i+1];
					if (first) {
						prev_product_id = product_id;
					}
				}
				else if (tokens[i].equals("product/title")) {
					product_name = ConvertArrayToString(tokens);
					if (first) {
						prev_product_name = product_name;
						first = false;
					}
				}
				else if (tokens[i].equals("review/summary")) {
					product_review_summary = ConvertArrayToString(tokens);
				}
				else if (tokens[i].equals("review/text")) {
					product_review_text = ConvertArrayToString(tokens);
					if (!prev_product_name.equals(product_name) && prev_product_id != product_id) {
						String temp_product_info = prev_product_id + " " + prev_product_name + " ||";
						context.write(new Text(temp_product_info), new Text(temp_product_reviews));
						prev_product_id = product_id;
						prev_product_name = product_name;
						//product_id = "";
						//product_name = "";
						//product_review_summary = "";
						//product_review_text = "";
						temp_product_reviews = "";	
					}
					temp_product_reviews += " "+ product_review_summary + " " + product_review_text + " <end>";	
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
	
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();

		Job job = new Job(conf, "ProductIdToNameMapping");
		job.setJarByClass(ExtractInfoMR.class);

		job.setMapperClass(Map.class);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.waitForCompletion(true);
	}

}