
/*
 *  An AAAA Record Object
 *   - represents an AAAA Record
 */

public class Record_AAAA extends Record {
	
	// String in HEX
	// eg 2001:0503:a83e:0000:0002:0030
	String ipv6;
	
	public Record_AAAA(String name, String type, String clas, String ttl, String rdata) {
		super(name, type, clas, ttl);
		ipv6 = parseIPV6address(rdata);
		Type = "AAAA";
	}
	
	
	// parses out an IPV6 address from the given RDATA section
	// Returns a String in standard IPV6 format e.g. 2001:503:a83e:0:2:30
	private String parseIPV6address(String rdata){
		
		String ipv6Address = "";
		
		while(rdata.length() > 0){
			String octet = rdata.substring(0, 4);
			
			if(octet.equals("0000")){
				octet = "0";
			}
			
			while(octet.startsWith("0") & octet.length() > 1){
				octet = octet.substring(1);
			}
			
			if(ipv6Address.equals("")){
				ipv6Address = octet;
			}else{
				ipv6Address = ipv6Address + ":" + octet;
			}
			
			rdata = rdata.substring(4);
		}
		
		return ipv6Address;
	}

}
