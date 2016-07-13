import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.xml.bind.DatatypeConverter;

	
// ----------------------------------------------------
// -     Built only for initial testing purposes      -
// -     Now depreciated. Use DNSlookup instead.	  -
// ----------------------------------------------------


public class Sender{
	
	int port;
	InetAddress address;
	DatagramSocket socket = null;
	DatagramPacket packet;
	byte[] sendBuf = new byte[512];

	public static void main(String [] args){
		try {
			// Send DNS Datagram
			DatagramSocket socket = new DatagramSocket();
			
			// Create Packet to Send
			QueryPacket sent = new QueryPacket("www.ugrad.cs.ubc.ca");
			
			// IP address of a DNS server in Vancouver
			InetAddress address = InetAddress.getByName("216.113.200.212");
			
			// Create and Send the packet
			DatagramPacket packetToSend = new DatagramPacket(sent.ALLbyte, sent.ALLbyte.length, address, 53);
			socket.send(packetToSend);
			
			// Receive DNS Datagram
			
			// Create packet to receive Query Answer
			// Max size of packet is 512
			byte [] bufToRec = new byte[512];
			DatagramPacket packetToRec = new DatagramPacket(bufToRec, bufToRec.length);
			
			// Receive Answer
			socket.receive(packetToRec);
			
			// Create a RecievedPacket object using received data
			ResponsePacket recPacket = new ResponsePacket(packetToRec);
			
			//Display Full Query Answer in Hex
			System.out.println(recPacket.ALLhex);
			
			socket.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}