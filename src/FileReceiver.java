import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class FileReceiver implements Runnable {

	private InetAddress ip;
	private int port;
	private File subor;
	private long velkostSuboru;
	private boolean[] prijate;
	private boolean finish = false;
	private DatagramSocket soket;
	private DatagramPacket paket;
	private boolean timeOut;
	private int downloaded;
	private FileSenderGui fileSenderGui;
	private StringWriter writer;
	
	public FileReceiver(InetAddress ip, int port, File subor, long velkostSuboru, FileSenderGui fileSenderGui) {
		super();
		this.ip = ip;
		this.port = port;
		this.subor = subor;
		this.velkostSuboru = velkostSuboru;
		prijate = new boolean[(int) (((velkostSuboru - 1) / FileSender.VELKOST_DAT_V_PAKETE) + 1)];
		this.fileSenderGui = fileSenderGui;
		writer = new StringWriter();
		fileSenderGui.addText("File receiver started \n");
	}

	@Override
	public void run() {
		RandomAccessFile raf = null;
		soket = null;
		try {
			boolean dataTecu = false;
			paket = null;
			long cas = System.nanoTime();
			int delta = 1000;
			while (!dataTecu) {
				soket = new DatagramSocket();

				byte[] buf = "posli".getBytes();
				paket = new DatagramPacket(buf, buf.length, ip, port);
				soket.send(paket);
				// cakanie na datove pakety
				buf = new byte[soket.getReceiveBufferSize()];
				paket = new DatagramPacket(buf, buf.length);
				soket.setSoTimeout(1000);
				try {
					soket.receive(paket);
					port = paket.getPort();
				} catch (SocketTimeoutException e) {
					continue;
				}
				dataTecu = true;
			}
			raf = new RandomAccessFile(subor, "rw");
			raf.setLength(velkostSuboru);
			delta = (int) (System.nanoTime() - cas) / 200000; 
			
			soket.setSoTimeout(delta);
			timeOut = false;
			while (!finish) {
				try {
					if(timeOut)
						soket.receive(paket);
					ByteArrayInputStream bais = new ByteArrayInputStream(
							paket.getData());
					ObjectInputStream ois = new ObjectInputStream(bais);
					long offset = ois.readLong();
					int poradiePaketu = (int) (offset / FileSender.VELKOST_DAT_V_PAKETE);
					prijate[poradiePaketu] = true;
					int datVPakete = (int) (Math.min(velkostSuboru - offset,
							FileSender.VELKOST_DAT_V_PAKETE));
					byte[] data = new byte[datVPakete];
					ois.read(data);
					raf.seek(offset);
					raf.write(data);
					downloaded++;
					//fileSenderGui.addText(String.valueOf((downloaded*FileSender.VELKOST_DAT_V_PAKETE*1.0)/velkostSuboru*100).toString());
					byte[] buf = new byte[soket.getReceiveBufferSize()];
					paket = new DatagramPacket(buf, buf.length);
					soket.receive(paket);
				} catch (SocketTimeoutException e) {
					waitForIt();
				}  

			}
			
		} catch (SocketException e) {
			try {
				waitForIt();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			finish = true;
			if (soket != null) {
				soket.close();
			}
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private void waitForIt() throws IOException {
		int[] offsets = getLost();
		int length = offsets.length;
		if (length == 0) {
			finish = true;
			length = -1;
			fileSenderGui.addText("koniec");
		} else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(Integer.SIZE
					* (length + 1));
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeInt(length);
			for (int i = 0; i < offsets.length; i++) {
				oos.writeInt(offsets[i]);
			}
			oos.flush();
			byte[] data = baos.toByteArray();
			DatagramPacket paket2 = new DatagramPacket(data, data.length, ip,
					port);
			soket.send(paket2);
			byte[] buf = new byte[soket.getReceiveBufferSize()];
			paket = new DatagramPacket(buf, buf.length);
			timeOut = true;
		}
	}

	private int[] getLost() {
		int[] offsets = new int[0];
		for (int i = 0; i < prijate.length; i++) {
			if (!prijate[i]) {
				int[] temp = Arrays.copyOf(offsets, offsets.length + 1);
				temp[offsets.length] = i;
				offsets = temp;
			}
			
			if(offsets.length==1000)
				return offsets;
		}
		return offsets;
	}

	public boolean getFinish() {
		return finish;
	}
}