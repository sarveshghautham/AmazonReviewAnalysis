import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

public class Cassandra
{
	private static Cluster cluster;
	private static Session session;
	private static Cassandra dbObj = null;
	private static String ipAddr = null;
	private static String keySpace = null;
	private static String tableName = null;
	private static int port = 0;
	private static Properties prop = null;

	private Cassandra()
	{
	}

	public static Cassandra getInstance()
	{
		if (dbObj == null)
			dbObj = new Cassandra();
		ipAddr = prop.getProperty("address");
		port = Integer.parseInt(prop.getProperty("port"));
		keySpace = prop.getProperty("keyspace");
		tableName = prop.getProperty("table");
		System.out.println(ipAddr + " " + port + " " + keySpace +  " " + tableName);
		cluster = Cluster.builder().addContactPoint(ipAddr).withPort(port)
				.build();
		Metadata metadata = cluster.getMetadata();
		System.out.printf("Connected to cluster: %s\n",
				metadata.getClusterName());
		for (Host host : metadata.getAllHosts())
			System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n",
					host.getDatacenter(), host.getAddress(), host.getRack());
		session = cluster.connect();
		return dbObj;
	}

	private static void loadProperties()
	{
		prop = new Properties();
		try
		{
			prop.load(new FileInputStream(
					"src/main/resources/Cassandra.properties"));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	private void dropKS()
	{
		String dropQuery = "DROP KEYSPACE IF EXISTS " + keySpace;
		System.out.println(dropQuery);
		session.execute(dropQuery);
	}

	private void createKS()
	{
		dropKS();
		String createQuery = "CREATE KEYSPACE "
				+ keySpace
				+ " WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }";
		System.out.println(createQuery);
		session.execute(createQuery);
	}

	private void dropTable()
	{
		String dropQuery = "DROP TABLE IF EXISTS " + keySpace + ".test";
		System.out.println(dropQuery);
		session.execute(dropQuery);
	}

	private void createTable()
	{
		dropTable();
		String createQuery = "CREATE TABLE " + keySpace
				+ ".test (product_id varchar PRIMARY KEY,product_name varchar)";
		System.out.println(createQuery);
		session.execute(createQuery);
	}
	
	public void connect(String node) {
	   cluster = Cluster.builder().withPort(9042)
	         .addContactPoint(node).
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
		session.execute(query);      
	}

	public String selectProduct(String productId)
	{
		/*
		 * String query =
		 * "SELECT positive_percentage, top_k_words FROM pds_ks.reviews where product_id='"
		 * + productId + "'";
		 */
		Statement statement = QueryBuilder.select().all()
				.from(keySpace, tableName).where(QueryBuilder.eq("PRODUCT_ID", productId));
		if(null == statement)
			System.out.println("Statement is null");
		if(null == session)
			System.out.println("Session is null");
		ResultSet result = session.execute(statement);
		StringBuilder results = new StringBuilder();
		for (Row row : result)
		{
			Double d = row.getDouble("positive_percentage");
			results.append(d.toString()).append("->")
					.append(row.getString("top_k_words"));
		}
		return results.toString();
	}

	public void close()
	{
		session.close();
		cluster.close();
	}
	
	public static void main (String []args) {
		Cassandra casObj = Cassandra.getInstance();
		
		String query_try = "INSERT INTO pds_ks.reviews (product_id, product_name, positive_percentage, top_k_words) VALUES ('B000FTPOMK', '14k Yellow Gold Butterfly Pendant, 16',69.00,'good:5, bad:3')";
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

		casObj.insertData(query_try);
		
	}
	
}
