
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Trevor Jackson - UBC ID:32478125 CS ID:n6n8 
 * @author Joshua Jackson - UBC ID:40601130 CS ID:r1c9
 * Adapted from code by Kurose, Ross & Donald Acton
 *
 */
public class DNSlookup {

	static final int MIN_PERMITTED_ARGUMENT_COUNT = 2;
	static boolean tracingOn = false;
	static String rootNameServer;
	static boolean debugOn = true;
	static int TTL = 999999999; // Keeps track of the last TTL seen, CNAME TTLs must be smaller than current TTL to replace current
	static int queryCount = 0;
	static ArrayList<Record_A> cacheARecords;
	static ArrayList<Record_AAAA> cacheAAAARecords;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String fqdn;

		int argCount = args.length;

		if (argCount == 3 && args[2].equals("-t")) {
			tracingOn = true;
		} else if (argCount == 3 && !args[2].equals("-t")) {
			usage();
			return;
		}

		if (argCount < 2 || argCount > 3) {
			usage();
			return;
		}
		
		if(checkValidIP(args[0])){
			rootNameServer = args[0];
		}else{
			usage();
			return;
		}
		
		fqdn = args[1];
		

		// Contains all A Records seen so far
		cacheARecords = new ArrayList<Record_A>();
		cacheAAAARecords = new ArrayList<Record_AAAA>();

		String ipAddr = lookup(fqdn, rootNameServer);
		
		if(ipAddr.equals("NULL")){
			System.out.println(fqdn + "  -4  0.0.0.0");
			return;
		}
		
		System.out.println(fqdn + "   " + TTL + "   " + ipAddr);

	}
	
	// Returns true if given string is in standard ipv4 format
	private static boolean checkValidIP(String ip){
		
		String[] split = ip.split("\\.");
		
		if (split.length != 4) {
			return false;
		} else if ((Integer.parseInt(split[0]) > 255) ||
				(Integer.parseInt(split[1]) > 255) || 
				(Integer.parseInt(split[2]) > 255) || 
				(Integer.parseInt(split[3]) > 255)) {
			return false;
		}
		
		String pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";
		Pattern p = Pattern.compile(pattern);
		
		Matcher m = p.matcher(ip);
		
		if(m.matches()){
			return true;
		}else{
			return false;
		}
		
	}

	private static void usage() {
		System.out.println("Usage: java -jar DNSlookup.jar rootDNS name [-t]");
		System.out.println("   where");
		System.out.println("       rootDNS - the IP address (in dotted form) of the root");
		System.out.println("                 DNS server you are to start your search at");
		System.out.println("       name    - fully qualified domain name to lookup");
		System.out.println("       -t      -trace the queries made and responses received");
	}

	public static void verbosePrint(Object obj) {
		if (debugOn) {
			System.out.println(obj);
		}
	}

	public static ResponsePacket query(String fqdn, String dnsServer){

		queryCount++;
		if(queryCount > 30){
			System.out.println(fqdn + "  -3  0.0.0.0");
			System.exit(1);
		}

		// Create socket
		try {
			DatagramSocket socket = new DatagramSocket();

			// Set socket Timeout to 5 seconds
			socket.setSoTimeout(5000);

			InetAddress server = InetAddress.getByName(dnsServer);

			// Create Packet to Send
			QueryPacket sent = new QueryPacket(fqdn);

			// Create and Send the packet
			DatagramPacket packet = new DatagramPacket(sent.ALLbyte, sent.ALLbyte.length, server, 53);
			socket.send(packet);

			// Create packet to receive Query Response
			// Max size of packet is 512 bytes
			byte [] bufToRec = new byte[512];
			DatagramPacket packetToRec = new DatagramPacket(bufToRec, bufToRec.length);

			// Receive Answer
			socket.receive(packetToRec);

			// Create a ResponsePacket object using received data
			ResponsePacket recieved = new ResponsePacket(packetToRec);

			// Verify that the Query IDs from the query and response match
			// If they don't, then keep waiting for correct packet to arrive
			while(!recieved.queryId.equals(sent.queryId)){
				socket.receive(packetToRec);

				// Create a ResponsePacket object using received data
				recieved = new ResponsePacket(packetToRec);
			}

			// Close Socket
			socket.close();

			// If the RCODE is 3 report a -1 error
			if(recieved.RCODE.equals("0011")){
				System.out.println(fqdn + "  -1  0.0.0.0");
				System.exit(1);
			}

			// If there is an RCODE of anything but 0 report a -4 error
			if(Integer.parseInt(ConversionFunctions.fromBinaryStringtoHexString(recieved.RCODE), 16) != 0) {
				System.out.println(fqdn + "  -4  0.0.0.0");
				System.exit(1);
			}

			if(tracingOn){

				boolean authoritative = false;
				if(recieved.AA.equals("1")){
					authoritative = true;
				}

				System.out.println("Query ID     " + ConversionFunctions.fromBinaryStringtoHexString(sent.queryId) + "  " + fqdn + "  --> " + dnsServer);
				System.out.println("Response ID: " + ConversionFunctions.fromBinaryStringtoHexString(recieved.queryId) +  "  Authoritative = " + authoritative);

				System.out.println("Answers (" + Integer.parseInt(ConversionFunctions.fromBinaryStringtoHexString(recieved.ANCOUNT), 16) + ")");
				for(Record entry: recieved.answerSection){
					System.out.format("       %-30s %-10d %-4s %s\n", entry.Name, Integer.parseInt(entry.TTL, 16), entry.Type, entry.Address);
				}

				System.out.println("Nameservers (" + Integer.parseInt(ConversionFunctions.fromBinaryStringtoHexString(recieved.NSCOUNT), 16) + ")");
				for(Record entry: recieved.nameserverSection){
					System.out.format("       %-30s %-10d %-4s %s\n", entry.Name, Integer.parseInt(entry.TTL, 16), entry.Type, entry.Address);
				}

				System.out.println("Additional Information (" + Integer.parseInt(ConversionFunctions.fromBinaryStringtoHexString(recieved.ARCOUNT), 16) + ")");
				for(Record entry: recieved.additionalSection){
					System.out.format("       %-30s %-10d %-4s %s\n", entry.Name, Integer.parseInt(entry.TTL, 16), entry.Type, entry.Address);
				}
				System.out.println("");
				System.out.println("");
			}
			
			
			
			return recieved;

		} catch (IOException e) {
			return null;
		}
	}


	// Recursively looks up fqdn at given DNS Server
	private static String lookup (String fqdn, String dnsServer){

		// Remove any expired Records from Cache
		updateCache();

		// Cached A Record
		if(!cacheARecords.isEmpty()){

			for(Record_A entry: cacheARecords){

				if(entry.Name.equals(fqdn)){

					// Retrieving a cached result
					// Need to increment the number of Queries done so far
					queryCount++;
					if(queryCount > 30){
						System.out.println(fqdn + "  -3  0.0.0.0");
						System.exit(1);
					}

					TTL = Integer.parseInt(entry.TTL, 16);
					return entry.ipv4;
				}

			}
		}

		// Cached AAAA Record
		if(!cacheAAAARecords.isEmpty()){

			for(Record_AAAA entry: cacheAAAARecords){

				if(entry.Name.equals(fqdn)){

					// Retrieving a cached result
					// Need to increment the number of Queries done so far
					queryCount++;
					if(queryCount > 30){
						System.out.println(fqdn + "  -3  0.0.0.0");
						System.exit(1);
					}

					TTL = Integer.parseInt(entry.TTL, 16);
					return entry.ipv6;
				}

			}
		}

		ResponsePacket response = query(fqdn, dnsServer);
		
		// Check if query timed out, retry if this is the first time out
		// if second time out then report -2 error
		if(response == null){
			response = query(fqdn, dnsServer);

			if(response == null){
				System.out.println(fqdn + "  -2  0.0.0.0");
				System.exit(1);
			}

		}

		// A Record Present
		ArrayList<Record> records = response.findRecord(fqdn, "A Records");
		if(!records.isEmpty()){

			for(Record entry: records){
				Record_A a = (Record_A) entry;

				TTL = Integer.parseInt(entry.TTL, 16);

				return a.ipv4;
			}
		}

		// AAAA Record Present
		records = response.findRecord(fqdn, "AAAA Records");
		if(!records.isEmpty()){

			for(Record entry: records){
				Record_AAAA aaaa = (Record_AAAA) entry;

				int currentTTL = Integer.parseInt(entry.TTL, 16);
				if(currentTTL < TTL){
					TTL = currentTTL;
				}

				return aaaa.ipv6;
			}
		}

		// CNAME Record Present
		records = response.findRecord(fqdn, "CNAME Records");
		if(!records.isEmpty()){

			Record_CNAME entry = (Record_CNAME) records.get(records.size() - 1);

			int currentTTL = Integer.parseInt(entry.TTL, 16);
			if(currentTTL < TTL){
				TTL = currentTTL;
			}

			return lookup(entry.CNAME, rootNameServer);
		}

		// NS Record Present
		if(!response.nsRecords.isEmpty()){

			Record_NS entry = response.nsRecords.get(response.nsRecords.size() - 1);
			return lookup(fqdn, lookup(entry.NS, rootNameServer));
		}

		return "NULL";

	}

	// Removes any Records with expired TTLs from the Cache
	private static void updateCache(){

		// A Record Cache
		for(int i = cacheARecords.size() - 1; i >= 0; i--){

			int ttl = Integer.parseInt(cacheARecords.get(i).TTL, 16);
			long added = cacheARecords.get(i).TimeAdded.getTime();
			Date expireDate = new Date(added + ttl * 1000);

			if(expireDate.before(new Date())){
				cacheARecords.remove(i);
			}
		}

		// AAAA Record Cache
		for(int i = cacheAAAARecords.size() - 1; i >= 0; i--){

			int ttl = Integer.parseInt(cacheAAAARecords.get(i).TTL, 16);
			long added = cacheAAAARecords.get(i).TimeAdded.getTime();
			Date expireDate = new Date(added + ttl * 1000);

			if(expireDate.before(new Date())){
				cacheAAAARecords.remove(i);
			}
		}
	}
}


