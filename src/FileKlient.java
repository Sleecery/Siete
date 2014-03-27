import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFileChooser;

public class FileKlient implements Runnable {

	private String pathFrom;
	private String pathTo;
	private int port;
	private DatagramSocket soket;
	private FileSenderGui fileSenderGui;

	public FileKlient(String pathFrom, String pathTo, int port,
			FileSenderGui fileSenderGui) {
		this.pathFrom = pathFrom;
		this.pathTo = pathTo;
		this.port = port;
		this.fileSenderGui = fileSenderGui;
		soket = null;
	}

	@Override
	public void run() {
		String nazovSuboru = pathFrom;
		InetAddress ip;
		try {
			ip = InetAddress.getByName("localhost");

			File file = new File(pathTo);

			soket = new DatagramSocket();

			byte[] buf = nazovSuboru.getBytes();
			DatagramPacket paket = new DatagramPacket(buf, buf.length,
					InetAddress.getByName("localhost"), port);

			soket.send(paket);

			buf = new byte[soket.getReceiveBufferSize()];
			paket = new DatagramPacket(buf, buf.length);
			soket.setSoTimeout(500);
			try {
				soket.receive(paket);
				ByteArrayInputStream bais = new ByteArrayInputStream(
						paket.getData());
				ObjectInputStream ois = new ObjectInputStream(bais);
				long velkostSuboru = ois.readLong();
				fileSenderGui.addText(String.valueOf(velkostSuboru));
				//
				FileReceiver fileReceiver = new FileReceiver(ip,
						paket.getPort(), file, velkostSuboru,fileSenderGui);

				ExecutorService executorService = Executors
						.newCachedThreadPool();
				executorService.execute(fileReceiver);

				while (!fileReceiver.getFinish()) {
					Thread.sleep(1000);
				}
				executorService.shutdown();
				return;
			} catch (SocketTimeoutException e) {
				fileSenderGui.addText("Server neodpoveda");
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			if (soket != null) {
				soket.close();
			}
		}
	}

}