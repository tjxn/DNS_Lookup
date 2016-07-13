
/*
 *  A NS Record Object
 *   - represents a NS Record
 */

public class Record_NS extends Record {
	
		String NS;
		
	public Record_NS(String name, String type, String clas, String ttl, String rdata, ResponsePacket packet) {
		super(name, type, clas, ttl);
		NS = packet.decodeName(rdata, false);
		Type = "NS";
	}

}

//	NS RDATA format
//	
//	+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//	/                   NSDNAME                     /
//	/                                               /
//	+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//	
//	where:
//	
//	NSDNAME     A <domain-name> which specifies a host which should be
//	            authoritative for the specified class and domain.