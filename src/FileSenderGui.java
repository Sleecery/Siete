import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JComboBox;
import javax.swing.JTextPane;
import javax.swing.JList;
import javax.swing.JEditorPane;

public class FileSenderGui extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JEditorPane editorPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FileSenderGui frame = new FileSenderGui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FileSenderGui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		final ExecutorService executorService = Executors.newCachedThreadPool();
		final FileSenderGui fileSenderGui = this;

		JToggleButton tglbtnStartServer = new JToggleButton("Start server");
		tglbtnStartServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				executorService.execute(new FileServer(fileSenderGui));
			}
		});
		tglbtnStartServer.setBounds(12, 12, 167, 25);
		contentPane.add(tglbtnStartServer);

		JLabel lblPathFrom = new JLabel("Path from:");
		lblPathFrom.setBounds(12, 49, 100, 15);
		contentPane.add(lblPathFrom);

		textField = new JTextField();
		textField.setBounds(104, 49, 328, 19);
		contentPane.add(textField);
		textField.setColumns(10);
		textField.setText("/home/filip/workspace/Siete/obrazok.jpg");
		JLabel lblPathTo = new JLabel("Path to:");
		lblPathTo.setBounds(12, 76, 70, 15);
		contentPane.add(lblPathTo);

		textField_1 = new JTextField();
		textField_1.setBounds(104, 74, 328, 19);
		contentPane.add(textField_1);
		textField_1.setColumns(10);
		textField_1.setText("/home/filip/workspace/Siete/obrazok2.jpg");
		JButton btnStartDownload = new JButton("Start download");
		btnStartDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				executorService.execute(new FileKlient(textField.getText(),
						textField_1.getText(), Integer.parseInt(textField_2
								.getText()), fileSenderGui));
			}
		});
		btnStartDownload.setBounds(12, 136, 167, 25);
		contentPane.add(btnStartDownload);

		JLabel lblPort = new JLabel("Port:");
		lblPort.setBounds(12, 103, 70, 15);
		contentPane.add(lblPort);

		textField_2 = new JTextField();
		textField_2.setBounds(104, 105, 328, 19);
		contentPane.add(textField_2);
		textField_2.setColumns(10);
		textField_2.setText("4567");

		editorPane = new JEditorPane();
		editorPane.setBounds(12, 173, 420, 188);
		contentPane.add(editorPane);
	}

	public void addText(String string) {
		editorPane.setText(editorPane.getText() +"\n" + string);
	}
}
