import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileServer implements Runnable {

	private DatagramSocket soket;
	private ExecutorService vyrabacVlakien;
	private DatagramPacket paket;
	private byte[] poles;
	private FileSenderGui fileSenderGui;

	/**
	 * @param args
	 * @throws IOException
	 */

	public FileServer(FileSenderGui fileSenderGui) {
		soket = null;
		this.fileSenderGui = fileSenderGui;
		vyrabacVlakien = Executors.newCachedThreadPool();
		try {
			soket = new DatagramSocket(4567);
			poles = new byte[soket.getReceiveBufferSize()];
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		paket = new DatagramPacket(poles, poles.length);
	}

	@Override
	public void run() {
		try {
			soket.receive(paket);
			String nazovSuboru = new String(paket.getData()).trim();
			File subor = new File(nazovSuboru);
			fileSenderGui.addText(nazovSuboru);
			if (subor.isFile()) {
				fileSenderGui.addText(paket.getAddress() + " chce subor "
						+ nazovSuboru);
				FileSender fileSender = new FileSender(subor,
						paket.getAddress(), paket.getPort(),fileSenderGui);
				vyrabacVlakien.execute(fileSender);
			} else {
				fileSenderGui.addText(paket.getAddress() + " chce subor "
						+ nazovSuboru + ", bohuzial ho neviem najst ...");
			}
			poles = new byte[soket.getReceiveBufferSize()];
			paket = new DatagramPacket(poles, poles.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}