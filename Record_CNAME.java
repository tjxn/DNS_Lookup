
/*
 *  A CNAME Record Object
 *   - represents a CNAME Record
 */

public class Record_CNAME extends Record {
	
	String CNAME;
	
	public Record_CNAME(String name, String type, String clas, String ttl, String rdata, ResponsePacket packet) {
		super(name, type, clas, ttl);
		CNAME = packet.decodeName(rdata, false);
		Type = "CNAME";
	}

}

//	CNAME RDATA format
//	
//	+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//	/                     CNAME                     /
//	/                                               /
//	+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//	
//	where:
//	
//	CNAME       A <domain-name> which specifies the canonical or primary
//				name for the owner.  The owner name is an alias.