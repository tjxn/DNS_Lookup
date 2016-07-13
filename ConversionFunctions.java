import javax.xml.bind.DatatypeConverter;

/*
 * 
 * Useful Helper Functions
 * 
 * A Binary String has the form: "01101100001101010"
 * A Hex String has the form: "0766696e636506676f6f676502636100"
 * An Ascii String has the form: "This is an ASCII string"
 * 
 */


public class ConversionFunctions {
	
	public static String fromHexStringtoBinaryString(String s) {
		byte[] result = fromHexStringtoByteArray(s);
		return fromByteArraytoBinaryString(result);
	}
	
	
	public static String fromByteArraytoBinaryString(byte[] data) {
		String	stringData = "";
		
		for(byte b : data){
			stringData = stringData + Integer.toBinaryString(b & 255 | 256).substring(1);
		}
		
		return stringData;
	}
	
	
	// Converts every 4 bits into a hex character
	// If there are 3 or fewer bits left over, drop them
	public static String fromBinaryStringtoHexString(String s) {
		String hexStr = "";
		
		while(s.length() >= 4){
			String subS = s.substring(0, 4);
			s = s.substring(4);
			Long decimal =  Long.parseLong(subS, 2);
			hexStr = hexStr + Long.toString(decimal, 16).toUpperCase();
		}
		
		return hexStr;
	}
	
	
	public static byte[] fromHexStringtoByteArray(String s) {
	    return DatatypeConverter.parseHexBinary(s);
	}
	
	
	public static String fromByteArraytoHexString(byte[] array) {
	    return DatatypeConverter.printHexBinary(array);
	}

	
	public static String fromHexStringtoAsciiString(String hex) {
		
		StringBuilder output = new StringBuilder();
	    for (int i = 0; i < hex.length(); i+=2) {
	        String str = hex.substring(i, i+2);
	        output.append((char)Integer.parseInt(str, 16));
	    }
	    
	    return output.toString();
	}

	
	public static String fromBinaryStringtoAsciiString(String binary){
		String hex = fromBinaryStringtoHexString(binary);
		return fromHexStringtoAsciiString(hex);
		
	}

	
	public static String fromByteArraytoAsciiString(byte[] array){
		String hex = fromByteArraytoHexString(array);
		return fromHexStringtoAsciiString(hex);
		
	}


	public static byte[] fromBinaryStringtoByteArray(String s) {
		String result = fromBinaryStringtoHexString(s);
		return fromHexStringtoByteArray(result);
	}
}
