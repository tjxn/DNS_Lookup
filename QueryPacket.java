import java.util.Random;


public class QueryPacket {
	
	// Individual Sections of the Query
	// Each Variable is a Binary String
	String queryId;
	String QR;
	String Opcode;
	String AA;
	String TC;
	String RD;
	String RA;
	String RCODE;
	String QDCOUNT;
	String ANCOUNT;
	String NSCOUNT;
	String ARCOUNT;
	String QNAME;
	String QTYPE;
	String QCLASS;
	
	// Complete Query in Specified Form
	String ALLhex;
	byte[] ALLbyte;
	String ALLbinary;
	
	// address is the human readable fqdn that you want the ip address of
	public QueryPacket(String address){
		
		// Generate unique ID
		byte[] id = new byte[2];
		Random randomGenerator = new Random();
		randomGenerator.nextBytes(id);
		
		queryId = ConversionFunctions.fromByteArraytoBinaryString(id);
		
		QR = "0";						// 1 bit indicating if a query (0) or a response (1)
		Opcode = "0000";				// Opcode - 4 bits will always be 0 to mean a standard query
		AA = "0";						// 1 bit that indicates if a response is authoritative
		TC = "0";						// 1 bit if set indicates the response is truncated
		RD = "0";						// 1 bit indicating if a query wants the name server to try to answer the question by indicating a recursive query
		RA = "0";						// 1 bit indicating if the responding server is capable of recursive queries
		RCODE = "0000";					// 4 bits of response code. 0000 means no error
		QDCOUNT = "0000000000000001";	// Query count
		ANCOUNT = "0000000000000000";	// Answer count
		NSCOUNT = "0000000000000000";	// Name Server Records
		ARCOUNT = "0000000000000000";	// Authority Record count
		QTYPE = "0000000000000001";		// Query type
		QCLASS = "0000000000000001";	// Query class
		
		
		QNAME = encodeQNAME(address);
		
		ALLbinary = queryId + QR + Opcode + AA + TC + RD + RA + "000" + RCODE + QDCOUNT + ANCOUNT + NSCOUNT + ARCOUNT
				+ QNAME + QTYPE + QCLASS;
		
		ALLhex = ConversionFunctions.fromBinaryStringtoHexString(ALLbinary);
		
		ALLbyte = ConversionFunctions.fromHexStringtoByteArray(ALLhex);
		
	}
	
	// address is a fqdn
	// will convert address from an ascii fqdn into binary equivalent
	// that follows DNS protocol for QNAME ie . removed and octets counts inserted
	private static String encodeQNAME(String address){
		int count = 0;
		String hexResult = "";
		String hexBuilder = "";
		
		while(address.length() > 0){
			if(address.startsWith(".")){
				hexResult = hexResult + String.format("%02x", count);
				hexResult = hexResult + hexBuilder;
				hexBuilder = "";
				count = 0;
			}else{
				count = count + 1;
				hexBuilder = hexBuilder + String.format("%02x", (int) address.charAt(0));
			}

			address = address.substring(1);
		}
		
		hexResult = hexResult + String.format("%02x", count) + hexBuilder + String.format("%02x", 0);
		
		return ConversionFunctions.fromHexStringtoBinaryString(hexResult);
	}
}
