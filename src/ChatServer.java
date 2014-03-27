

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ChatServer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		DatagramSocket soket=null;
		System.out.println("Server started");
		try {
			 soket= new DatagramSocket(4567);
			 byte[] pole =new byte[soket.getReceiveBufferSize()];
			 DatagramPacket paket=new DatagramPacket(pole, pole.length);
			 while(true){
			 soket.receive(paket);
			 String sprava = new String(paket.getData());
			 System.out.println(paket.getAddress()+": " + sprava);
			 pole =new byte[soket.getReceiveBufferSize()];
			 paket=new DatagramPacket(pole, pole.length);
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