/*
 *  An A Record Object
 *   - represents an A Record
 */


public class Record_A extends Record {
	
	// String containing Decimal eg 192.141.22.1
	// TODO Do we want this to be in a different format?
	String ipv4;
	
	public Record_A(String name, String type, String clas, String ttl, String rdata) {
		super(name, type, clas, ttl);
		ipv4 = parseIPV4address(rdata);
		Type = "A";
	}
	
	
	// parses out an IPV4 address from the given RDATA section
	// Returns a String in standard IPV4 format e.g. 192.233.1.2
	private String parseIPV4address(String rdata){
		String ipv4Address = "";
		
		while(rdata.length() > 0){
			String octet = rdata.substring(0, 2);
			
			if(ipv4Address.equals("")){
				ipv4Address = Integer.toString(Integer.parseInt(octet, 16));
			}else{
				ipv4Address = ipv4Address + "." + Integer.toString(Integer.parseInt(octet, 16));
			}
			
			rdata = rdata.substring(2);
		}
		
		return ipv4Address;
	}

	
	
}

//	A RDATA format
//	
//	+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//	|                    ADDRESS                    |
//	+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//	
//	where:
//	
//	ADDRESS         A 32 bit Internet address.
//	
//	Hosts that have multiple Internet addresses will have multiple A
//	records.