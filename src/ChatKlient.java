

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ChatKlient {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		DatagramSocket soket=null;
		try {
			 soket= new DatagramSocket();
			 Scanner citac=new Scanner(System.in);
			 String sprava=citac.nextLine();
			 while(! "exit".equals(sprava)){
				 byte[] buf=sprava.getBytes();
				 DatagramPacket paket = 
						 new DatagramPacket(buf, buf.length, InetAddress.getByName("255.255.255.255"), 4567);
				 
				 soket.send(paket);
				 sprava= citac.nextLine();
			 }
			
			 } catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if (soket!=null){
				soket.close();
			}
		}

	
	}

}