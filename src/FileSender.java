import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class FileSender implements Runnable {

	public static final int VELKOST_DAT_V_PAKETE = 1000;
	private File subor;
	private InetAddress ip;
	private int port;
	private RandomAccessFile raf;
	private DatagramSocket soket;
	private boolean notDone = true;
	private byte[] bufs;
	private DatagramPacket paket;
	private FileSenderGui fileSenderGui;

	public FileSender(File subor, InetAddress ip, int port,FileSenderGui fileSenderGui) {
		this.subor = subor;
		this.ip = ip;
		this.port = port;
		this.fileSenderGui = fileSenderGui;
	}

	public @Override
	void run() {

		try {
			soket = new DatagramSocket();

			long velkostSuboru = subor.length();
			ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeLong(velkostSuboru);
			oos.flush();
			bufs = baos.toByteArray();
			DatagramPacket paket = new DatagramPacket(bufs, bufs.length, ip,
					port);

			soket.send(paket);
			// cakam na spravu "posli"
			bufs = new byte[soket.getReceiveBufferSize()];
			paket = new DatagramPacket(bufs, bufs.length);
			soket.setSoTimeout(3000);
			try {
				soket.receive(paket);
			} catch (SocketTimeoutException e) {
				return;
			}
			// overenie ci je to poslanie od spravnej ip
			port = paket.getPort();
			// ip = paket.getAddress(); FTIP, JOKE, ANEKDOTA
			raf = new RandomAccessFile(subor, "r");
			int i = 0;
			for (long offset = 0; offset < velkostSuboru; offset += VELKOST_DAT_V_PAKETE) {
				posliCastSuboru(offset, VELKOST_DAT_V_PAKETE);
			}

			// cakame na chybajuce one veci

			while (notDone) {
				long[] missing = getMissing();
				if (null != missing)
					sendMissing(missing);
				else
					Thread.sleep(10);
			}

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (soket != null) {
				soket.close();
			}
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private void sendMissing(long[] missing) {
		for (int i = 0; i < missing.length; i++) {
			posliCastSuboru(missing[i], VELKOST_DAT_V_PAKETE);
		}
	}

	private long[] getMissing() {
		try {
			bufs = new byte[soket.getReceiveBufferSize()];
			paket = new DatagramPacket(bufs, bufs.length);
			soket.receive(paket);
			ByteArrayInputStream bais = new ByteArrayInputStream(
					paket.getData());
			ObjectInputStream ois = new ObjectInputStream(bais);
			int num = ois.readInt();
			if (num == -1) {
				notDone = false;
				return null;
			}
			int[] missing = new int[num];
			for (int i = 0; i < num; i++) {
				missing[i] = ois.readInt();
			}
			return getOffsets(missing);
		} catch (SocketTimeoutException e) {
			fileSenderGui
					.addText("File receiver doesnt need more files, finishing ...");
			notDone = false;
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private long[] getOffsets(int[] missing) {
		long[] offsets = new long[missing.length];
		for (int i = 0; i < missing.length; i++) {
			offsets[i] = missing[i] * FileSender.VELKOST_DAT_V_PAKETE;
		}
		return offsets;
	}

	private void posliCastSuboru(long offset, int dlzka) {
		byte[] dataSuboru = new byte[dlzka];
		try {
			raf.seek(offset);
			dlzka = raf.read(dataSuboru);
			ByteArrayOutputStream baos = new ByteArrayOutputStream(dlzka
					+ Long.SIZE /* vrati 8 */);
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeLong(offset);
			oos.write(dataSuboru, 0, dlzka);
			oos.flush();
			byte[] data = baos.toByteArray();
			DatagramPacket paket = new DatagramPacket(data, data.length, ip,
					port);
			soket.send(paket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}