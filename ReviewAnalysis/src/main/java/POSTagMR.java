import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.StringEscapeUtils;
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
		private NLPParser nlpObj = NLPParser.getInstance();
		private Cassandra cObj =  Cassandra.getInstance();
		
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			String line = value.toString();
			String []tokens = line.split(" ");
			
			String productId = tokens[1];
			String productName = ExtractProductName(tokens);
			String reviews = ConvertArrayToString(tokens);
			//ArrayList<String> adjectives = POSTagger.POSTaggedWords(reviews);
			NlpResult nlpResult = nlpObj.parseText(reviews);
			ArrayList<String> adjectives = nlpResult.adjectives;
			HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
			
			//String newKey = productId + " " + productName + " " + word;
			
			for (String word : adjectives) {
				//String newKey = productId + " " + productName + " " + word;		
				//context.write(new Text(newKey), one);
				int count = 0;
				if (wordCount.containsKey(word)) {
					count = wordCount.get(word);					
				}
				count++;
				wordCount.put(word, count);
			}
			
			LinkedHashMap<String, Integer> sortedMap = sortHashMapByValuesD(wordCount);
			
			Set<String> keys = sortedMap.keySet();
			String topKWords = "";
			
	        for(String k:keys){
	            //System.out.println(k+"-"+sortedMap.get(k));
	            topKWords += k+":"+sortedMap.get(k)+"||";
	        }
	        
	        //Insert into cassandra
	        productName = StringEscapeUtils.escapeSql(productName);
	        topKWords = StringEscapeUtils.escapeSql(topKWords);
	        
	        String query = "INSERT INTO pds_ks.reviews (product_id, product_name, positive_percentage, top_k_words) VALUES ('"+productId+"','"+productName+"',"+nlpResult.posPercent+",'"+topKWords+"')";
	        System.out.println(query);
	        cObj.insertData(query);
	        topKWords = "";
		}
		
		public LinkedHashMap<String, Integer> sortHashMapByValuesD(HashMap<String, Integer> passedMap) {
		   List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
		   List<Integer> mapValues = new ArrayList<Integer>(passedMap.values());
		   Collections.sort(mapValues, Collections.reverseOrder());		   
		   Collections.sort(mapKeys);

		   LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();

		   Iterator<Integer> valueIt = mapValues.iterator();
		   while (valueIt.hasNext()) {
		       Object val = valueIt.next();
		       Iterator<String> keyIt = mapKeys.iterator();

		       while (keyIt.hasNext()) {
		           Object key = keyIt.next();
		           String comp1 = passedMap.get(key).toString();
		           String comp2 = val.toString();

		           if (comp1.equals(comp2)){
		               passedMap.remove(key);
		               mapKeys.remove(key);
		               sortedMap.put((String)key, (Integer)val);
		               break;
		           }

		       }

		   }
		   return sortedMap;
		}
		
		public String ExtractProductName (String []tokens) {
			StringBuilder sb = new StringBuilder();
			int i = 2;
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

		Job job = new Job(conf, "POSTagMR");
		job.setJarByClass(POSTagMR.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(Map.class);
		//job.setReducerClass(Reduce.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.waitForCompletion(true);
	}

}