import java.util.ArrayList;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;

public class Cassandra {
	private Cluster cluster;
	private static Cassandra dbObj = null;
	
	private Cassandra () {
		
	}
	public static Cassandra getInstance() {
		if(dbObj == null){	
			dbObj = new Cassandra();
		}
		dbObj.connect("127.0.0.1");
		
		return dbObj;
	}
	protected void finalize() {
		close();
	}
	
	public void connect(String node) {
	   cluster = Cluster.builder()
	         .addContactPoint(node).withPort(9042).
	         build();
	   
	   
	   Metadata metadata = cluster.getMetadata();
	   System.out.printf("Connected to cluster: %s\n", 
	         metadata.getClusterName());
	   for ( Host host : metadata.getAllHosts() ) {
	      System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n",
	         host.getDatacenter(), host.getAddress(), host.getRack());
	   }	   
	}
	
	public void insertData (String query) {
		Session session = cluster.connect();
		System.out.println(query);
		session.execute(query);      
	}
	
	public ArrayList<Integer> selectQuery (String matchTag) {
		Session session = cluster.connect();
		String query = "SELECT * FROM ks_hashscore.hashscore where match_tag='"+matchTag+"' ORDER BY end_time DESC LIMIT 1";
		ResultSet result = session.execute(query);
		ArrayList<Integer> temp = new ArrayList<Integer>();
		
		for (Row row: result) {
			temp.add(row.getInt("fours_count"));
			temp.add(row.getInt("sixers_count"));
			temp.add(row.getInt("wickets_count"));
		}
		return temp;
	}
	
	public void close () {
		cluster.close();
	}
	
//	public static void main (String []args) {
//		Cassandra casObj = new Cassandra();
//		
//		String query_try = "INSERT INTO pds_ks.id_to_name (product_id, product_name) VALUES ('B000FTPOMK', '14k Yellow Gold Butterfly Pendant, 16')";
//		String query_try = "UPDATE pds_ks.id_to_name SET product_name='14k' WHERE product_id='B000FTPOMK'";
//		String updateQuery = "UPDATE pds_ks.id_to_name "
//				+ "SET product_name ='"+product_name+"' "
//						+ "WHERE product_id='"+product_id+"'";
//		casObj.insertData(updateQuery);
//		
//		String product_id = "B000FTPOMK";
//		String product_name = "'14k Yellow Gold Butterfly Pendant, 16";
//		
//		String updateQuery = "UPDATE pds_ks.id_to_name "
//		+ "SET product_name ='"+product_name+"' "
//				+ "WHERE product_id='"+product_id+"'";
//
//		casObj.insertData(query_try);
		
//	}
	
}
