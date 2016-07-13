import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.net.InetAddress;

import javax.xml.bind.DatatypeConverter;


public class ResponsePacket {

	// Variables
	// Each variable is a string containing binary, unless specified otherwise
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
	String RDATA;
	
	//
	ArrayList<Record> answerSection;
	ArrayList<Record> nameserverSection;
	ArrayList<Record> additionalSection;
	
	// Contains all the Records of the appropriate type from that Packet
	ArrayList<Record_A> aRecords;
	ArrayList<Record_AAAA>  aaaaRecords;
	ArrayList<Record_CNAME> cnameRecords;
	ArrayList<Record_NS> nsRecords;
	
	// Entire Packet in particular format
	String ALLbinary;
	String ALLhex;
	byte [] ALLbyte;
	
	// The current position within the DatagramPacket that separates the
	// processed and unprocessed data 
	// i.e. If only the queryId variable had been set then EndPosition would be 16
	// because the queryId is the first 16 bits
	int EndPosition = 0;
	
	
	public ResponsePacket(DatagramPacket packet){
		
		// Convert DatagramPacket data into a Binary String
		byte[] data = packet.getData();
		String stringData = ConversionFunctions.fromByteArraytoBinaryString(data);
		
		// Initialize Lists
		// Will contain all Records from the packet
		aRecords = new ArrayList<Record_A>();
		aaaaRecords = new ArrayList<Record_AAAA>();
		nsRecords = new ArrayList<Record_NS>();
		cnameRecords = new ArrayList<Record_CNAME>();
		
		// Initialize Lists
		// Will contain the records of that section
		answerSection = new ArrayList<Record>();
		nameserverSection = new ArrayList<Record>();
		additionalSection = new ArrayList<Record>();
		
		// Unless otherwise specified, each of these variables contains Binary
		queryId = stringData.substring(0, 16); 		// ID - 16 bit identifier
		QR = stringData.substring(16, 17);			// QR - 1 bit, 0 is a query, 1 is a response
		Opcode = stringData.substring(17, 21);		// OPCODE - 4 bits, type of query
		AA = stringData.substring(21, 22);			// AA - 1 bit, responding server is an authority for domain name
		TC = stringData.substring(22, 23);			// TC - 1 bit, specify if message was truncated
		RD = stringData.substring(23, 24);			// RD - 1 bit, Recursion Desired
		RA = stringData.substring(24, 25);			// RA - 1 bit, Recursion Available
		RCODE = stringData.substring(28, 32);		// RCODE - 4 bits, Error Codes
		QDCOUNT = stringData.substring(32, 48);		// QDCOUNT - 16 bits, number of entries in Question Section
		ANCOUNT = stringData.substring(48, 64);		// ANCOUNT - 16 bits, number of Resource Records in Answer Section
		NSCOUNT = stringData.substring(64, 80);		// NSCOUNT - 16 bits, number of Resource Records in Authority Records Section
		ARCOUNT = stringData.substring(80, 96);		// ARCOUNT - 16 bits, number of Resource Records in Additional Records Section
		
		// Update EndPosition to Reflect that the first 96 bits have been processed now
		EndPosition += 96;
		
		// Question Section
		QNAME = decodeName(ConversionFunctions.fromBinaryStringtoHexString(stringData.substring(96)), true);
		QTYPE = stringData.substring(EndPosition, EndPosition + 16);
		QCLASS = stringData.substring(EndPosition + 16, EndPosition + 32);
		EndPosition += 32;
		
		ALLbinary = stringData;
		ALLhex = ConversionFunctions.fromBinaryStringtoHexString(ALLbinary);
		ALLbyte = ConversionFunctions.fromBinaryStringtoByteArray(ALLbinary);
		
		// Process data from the Answer, Authority Records and Additional Records Sections
		decodeAnswerAuthAdd(ConversionFunctions.fromBinaryStringtoHexString(stringData.substring(EndPosition)));
	}
	
	
	// Returns all the Records with a given name in an ArrayList
	// record is a string with value:
	// "A Records"
	// "AAAA Records"
	// "CNAME Records"
	// "NS Records"
	// specifying what kind of record to look for
	public ArrayList<Record> findRecord(String name, String record){
		
		ArrayList<Record> entries = new ArrayList<Record>();
		
		if(record.equals("A Records")){
			
			for(Record_A entry: aRecords){
				if(entry.Name.equals(name)){
					entries.add(entry);
				}
			}
		}else if(record.equals("AAAA Records")){
			
			for(Record_AAAA entry: aaaaRecords){
				if(entry.Name.equals(name)){
					entries.add(entry);
				}
			}
		}else if(record.equals("NS Records")){
			
			for(Record_NS entry: nsRecords){
				if(entry.Name.equals(name)){
					entries.add(entry);
				}
			}
		}else if(record.equals("CNAME Records")){
			
			for(Record_CNAME entry: cnameRecords){
				if(entry.Name.equals(name)){
					entries.add(entry);
				}
			}
		}
		
		return entries;
	}
	
	
	// Given a Hex String
	// String starts at the beginning of the Answer/Auth/Additional Sections, right after Question Section
	// Parses out the Records from each section and adds them to the appropriate list (aRecords, aaaaRecords, nsRecords or cnameRecords)
	private void decodeAnswerAuthAdd(String hex){
		
		int AnswerCount = Integer.parseInt(ConversionFunctions.fromBinaryStringtoHexString(ANCOUNT), 16);
		int AuthoritativeCount = Integer.parseInt(ConversionFunctions.fromBinaryStringtoHexString(NSCOUNT), 16);
		int AdditionalCount = Integer.parseInt(ConversionFunctions.fromBinaryStringtoHexString(ARCOUNT), 16);	
		
		while(AnswerCount > 0){
			hex = parseSection(hex, "Answer");
			AnswerCount--;
		}
		
		while(AuthoritativeCount > 0){
			hex = parseSection(hex, "Nameserver");
			AuthoritativeCount--;
		}
		
		while(AdditionalCount > 0){
			hex = parseSection(hex, "Additional");
			AdditionalCount--;
		}
		
	}
	
	// Given a Hex String
	// String starts at the beginning of the Answer/Auth/Additonal Sections, right after Question Section
	// Will create either a Record A, AAA, CNAME or NS object and place it in the appropriate List
	private String parseSection(String hex, String section){
		
		String name = decodeName(hex, true);				// NAME, variable length
		hex = ALLhex.substring(((EndPosition) / 8) * 2);	// 
		String type = hex.substring(0, 4);					// TYPE, 2 octets
		String clas = hex.substring(4, 8);					// CLASS, 2 octets
		String TTL = hex.substring(8, 16);					// TTL, 32 bit signed integer, time interval RR may be cached
		String RDLength = hex.substring(16, 20);			// RDLENGTH, 16 bits, length in octets of RDATA
		EndPosition += (20/2)*8;							// Update EndPosition to reflect processed data
		
		int length = Integer.parseInt(RDLength, 16);
		String RData = hex.substring(20, 20 + (length * 2));// RDATA, variable length
		EndPosition += (length * 8);						// Update EndPosition to reflect processed data
		
		// Type A
		if(type.equals("0001")){
			Record_A a = new Record_A(name, type, clas, TTL, RData);
			aRecords.add(a);
			DNSlookup.cacheARecords.add(a);
			addSection(a, section, a.ipv4);
			
		// Type NS
		}else if(type.equals("0002")){
			Record_NS ns = new Record_NS(name, type, clas, TTL, RData, this);
			nsRecords.add(ns);
			addSection(ns, section, ns.NS);
			
		// Type AAAA
		}else if(type.equals("001C")){
			Record_AAAA aaaa = new Record_AAAA(name, type, clas, TTL, RData);
			aaaaRecords.add(aaaa);
			DNSlookup.cacheAAAARecords.add(aaaa);
			addSection(aaaa, section, aaaa.ipv6);

			// Type CNAME
		}else if(type.equals("0005")){
			Record_CNAME cname = new Record_CNAME(name, type, clas, TTL, RData, this);
			cnameRecords.add(cname);
			addSection(cname, section, cname.CNAME);

			// Unknown Type
		}else{
			Record unknown = new Record(name, type, clas, TTL);
			while(unknown.Type.startsWith("0") & (unknown.Type.length() != 1)) {
				unknown.Type = unknown.Type.substring(1);
			}
			unknown.Address = "----";
			addSection(unknown, section, unknown.Address);
		}

		return hex.substring(20 + (length * 2));
	}

	private void addSection(Record record, String section, String addr){
		record.Address = addr;

		if(section.equals("Answer")){
			answerSection.add(record);
		}else if(section.equals("Nameserver")){
			nameserverSection.add(record);
		}else if(section.equals("Additional")){
			additionalSection.add(record);
		}
		
	}
	
	// Given a Hex String starting at the Name, QNAME or RDATA field
	// Will return the first name it finds in ASCII String
	// Requires ALLhex to have been populated first
	// Boolean Argument specifies if EndPosition should be updated (True if calling on NAME, QNAME - False if calling on RDATA) 
	public String decodeName(String hex, boolean pos){
		
		String name = "";
		String offsetStr = "";
		int offset = 0;
		
		// Start of field (in bits) can be 3 things:
		// 00000000 00000000 - end of name
		// 11 - pointer to another name
		// xxxxxxxx xxxxxxxx - a number representing the number of octets before a period in the hostname
		while(!hex.startsWith("00")){
			
			// two 1 bits means start of compression, next 14 bits are the actual address
			if(ConversionFunctions.fromHexStringtoBinaryString(hex).startsWith("11")){
				
				// Get 6 bits that occur after the two 1 bits
				String offsetPart1 = ConversionFunctions.fromHexStringtoBinaryString(hex).substring(2, 8);
				
				// Remove the C0 from the hex string
				hex = hex.substring(2);
				
				// Offset for actual address is the first 6 bits of the first byte + the 8 bits of the second byte
				offsetStr = offsetPart1 + ConversionFunctions.fromHexStringtoBinaryString(hex.substring(0, 2));
				offset = Integer.parseInt(offsetStr, 2);
				
				// Used for formatting the name
				if(name != ""){
					name = name + "." + decodeName(ALLhex.substring(offset*2), false);
				}else{
					name = decodeName(ALLhex.substring(offset*2), false);
				}
				
				// Depending on how the function was called we may or may not want the EndPosition global variable updated
				if(pos){
					EndPosition += 8;
				}
				break;
				
			}else{
				offsetStr = hex.substring(0, 2);
				offset = Integer.parseInt(offsetStr, 16);
				if(pos){
					EndPosition = EndPosition + offset*8 + 8;
				}
				if(name == ""){
					name = ConversionFunctions.fromHexStringtoAsciiString(hex.substring(2, 2 + offset*2));
				}else{
					name = name + "." + ConversionFunctions.fromHexStringtoAsciiString(hex.substring(2, 2 + offset*2));
				}
				
				hex = hex.substring(2 + offset*2);
				
			}
		}
		
		if(pos){
			EndPosition = EndPosition + 8;
		}
		
		return name;
	}
	
}
