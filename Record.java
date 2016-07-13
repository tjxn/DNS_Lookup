import java.util.Date;


/*
 *  A Record Object
 *   - represents a generic Record
 */

public class Record {

	// Strings containing HEX
	String Name;
	String Type;
	String Class;
	String TTL;
	String Address; //Generic field used for printing out traces. Is a copy of the contents of ipv4/ipv6/NS/CNAME field
	Date TimeAdded; //The Date&Time that the Record was created

	public Record(String name, String type, String clas, String ttl){
		Name = name;
		Type = type;
		Class = clas;
		TTL = ttl;
		Address = "";
		TimeAdded = new Date();
	}

}

//	The answer, authority, and additional sections all share the same
//	format: a variable number of resource records, where the number of
//	records is specified in the corresponding count field in the header.
//	Each resource record has the following format:
//								  1  1  1  1  1  1
//	0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
//	+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//	|                                               |
//	/                                               /
//	/                      NAME                     /
//	|                                               |
//	+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//	|                      TYPE                     |
//	+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//	|                     CLASS                     |
//	+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//	|                      TTL                      |
//	|                                               |
//	+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//	|                   RDLENGTH                    |
//	+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--|
//	/                     RDATA                     /
//	/                                               /
//	+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//	
//	where:
//	
//	NAME            a domain name to which this resource record pertains.
//	
//	TYPE            two octets containing one of the RR type codes.  This
//					field specifies the meaning of the data in the RDATA
//					field.
//	
//	CLASS           two octets which specify the class of the data in the
//					RDATA field.
//	
//	TTL             a 32 bit unsigned integer that specifies the time
//					interval (in seconds) that the resource record may be
//					cached before it should be discarded.  Zero values are
//					interpreted to mean that the RR can only be used for the
//					transaction in progress, and should not be cached.