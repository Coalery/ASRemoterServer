import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class GUI extends JFrame {
	private Answer answer;
	
	protected static int addX, addY;
	
	protected boolean isFullScreen;
	protected boolean isAreaSelectMode;
	
	protected static Dimension clientSize;
	protected static JLabel drawLocation;
	
	public GUI() {
		super("A/S");
		
		System.out.println("Initialize GUI");
		
		answer = new Answer();
		answer.start();
		
		setLayout(new BorderLayout());
		isFullScreen = true;
		isAreaSelectMode = false;
		
		addX = 0;
		addY = 0;
		
		clientSize = new Dimension();
		
		addWindowListener(new WindowAdapter() {public void windowClosing(WindowEvent event) {
			try {new DataOutputStream(Main.socket.getOutputStream()).write(Answer.END);} catch (IOException e) {}
			System.exit(0);
		}});
		
		addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				if(isAreaSelectMode)
					return;
				answer.send(arg0.getKeyCode(), false);
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				if(isAreaSelectMode)
					return;
				answer.send(arg0.getKeyCode(), true);
			}
		});
		
		addMouseListener(new MouseAdapter() {
			int x, y, width, height;
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if(isAreaSelectMode) {
					int x2 = (int)((e.getX()-3) * (clientSize.getWidth() / drawLocation.getWidth()));
					int y2 = (int)((e.getY()-50) * (clientSize.getHeight() / drawLocation.getHeight()));
					
					width = Math.abs(x2-x);
					height = Math.abs(y2-y);
					
					answer.send(x, y, width, height);
					
					isFullScreen = false;
					isAreaSelectMode = false;
					addX = x;
					addY = y;
					
					return;
				}
				Point p = new Point(e.getX()-3, e.getY()-50);
				if(e.getButton() == 1)
					answer.send(p, InputEvent.BUTTON1_MASK, true);
				else if(e.getButton() == 3)
					answer.send(p, InputEvent.BUTTON3_MASK, true);
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(isAreaSelectMode) {
					x = (int)((e.getX()-3) * (clientSize.getWidth() / drawLocation.getWidth()));
					y = (int)((e.getY()-50) * (clientSize.getHeight() / drawLocation.getHeight()));
					
					return;
				}
				Point p = new Point(e.getX()-3, e.getY()-50);
				if(e.getButton() == 1)
					answer.send(p, InputEvent.BUTTON1_MASK, false);
				else if(e.getButton() == 3)
					answer.send(p, InputEvent.BUTTON3_MASK, false);
			}
		});
		
		addMouseWheelListener(new MouseWheelListener() {public void mouseWheelMoved(MouseWheelEvent arg0) {
			if(isAreaSelectMode)
				return;
			answer.send((arg0.getWheelRotation() == 1) ? true : false);
		}});
		
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent arg0) {
				if(isAreaSelectMode)
					return;
				answer.send(new Point(arg0.getX()-3, arg0.getY()-50));
			}
			
			@Override
			public void mouseDragged(MouseEvent arg0) {
				if(isAreaSelectMode)
					return;
				answer.send(new Point(arg0.getX()-3, arg0.getY()-50));
			}
		});
		
		JMenuBar mb = new JMenuBar();
		
		JMenu m = new JMenu("Functions");
		JMenuItem fileSend = new JMenuItem("파일 보내기");
		JMenuItem message = new JMenuItem("메시지 보내기");
		JMenu range = new JMenu("범위 지정");
		
		JMenuItem rangeSet_2 = new JMenuItem("직접 설정");
		JMenuItem rangeSet_3 = new JMenuItem("풀 스크린");
		
		JMenuItem voiceChat = new JMenuItem("통화 시작");
		
		voiceChat.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent event) {
			
		}});
		
		fileSend.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent event) {
			if(isAreaSelectMode)
				return;
			
			JFrame parent = new JFrame();
			FileDialog fd = new FileDialog(parent);
			fd.setVisible(true);
			parent.dispose();
			
			answer.send(new File(fd.getDirectory() + "\\" + fd.getFile()));
			
			parent.dispose();
		}});
		message.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent event) {
			if(isAreaSelectMode)
				return;
			JFrame f = new JFrame("���� �޼��� �Է�");
			f.setSize(300, 200);
			
			f.addWindowListener(new WindowAdapter() {public void windowClosing(WindowEvent event) {
				f.dispose();
				f.setVisible(false);
			}});
			
			JTextArea ta = new JTextArea();
			JScrollPane sp = new JScrollPane(ta);
			
			f.add(sp, "Center");
			
			JButton b = new JButton("����");
			b.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent event) {
				f.dispose();
				f.setVisible(false);
				
				answer.send(ta.getText());
			}});
			
			f.add(b, "South");
			
			f.setVisible(true);
			f.setResizable(false);
		}});
		
		rangeSet_2.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent event) {
			if(!isFullScreen) {
				JFrame tmp = new JFrame();
				JOptionPane.showMessageDialog(tmp, "Ǯ ��ũ�� ���¿����� ����� �� �ֽ��ϴ�.");
				tmp.dispose();
				return;
			}
			if(!isAreaSelectMode)
				isAreaSelectMode = true;
		}});
		
		rangeSet_3.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent event) {
			if(isAreaSelectMode)
				return;
			answer.sendSingleCommand(Answer.SET_FULL_SCREEN);
			setLocation(0, 0);
			isFullScreen = true;
			
			addX = 0;
			addY = 0;
		}});
		
		range.add(rangeSet_2);
		range.add(rangeSet_3);
		
		JMenu client = new JMenu("Client Info...");
		JMenuItem clientB = new JMenuItem("Show Client Information");
		JMenuItem database = new JMenuItem("Find Client Informations");
		
		clientB.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent event) {
			JFrame fr = new JFrame("Client Info");
			fr.addWindowListener(new WindowAdapter() {public void windowClosing(WindowEvent event) {
				fr.setVisible(false);
				fr.dispose();
			}});
			fr.setBounds(100, 100, 200, 250);
			fr.setLayout(null);
			
			JLabel lblF = new JLabel("\uC774\uB984");
			lblF.setBounds(12, 10, 57, 15);
			fr.add(lblF);
			
			JLabel label = new JLabel("\uC11C\uBE44\uC2A4");
			label.setBounds(12, 50, 57, 15);
			fr.add(label);
			
			JLabel label_1 = new JLabel("\uC544\uC774\uD53C");
			label_1.setBounds(12, 90, 57, 15);
			fr.add(label_1);
			
			JLabel label_2 = new JLabel("\uC8FC\uC18C");
			label_2.setBounds(12, 130, 57, 15);
			fr.add(label_2);
			
			JLabel label_3 = new JLabel("\uC11C\uBE44\uC2A4 \uC77C\uC790");
			label_3.setBounds(12, 170, 82, 15);
			fr.add(label_3);
			
			JLabel lblName = new JLabel(Main.name);
			lblName.setFont(new Font("����", Font.PLAIN, 12));
			lblName.setBounds(12, 25, 160, 15);
			fr.add(lblName);
			
			JLabel lblService = new JLabel(Main.service);
			lblService.setFont(new Font("����", Font.PLAIN, 12));
			lblService.setBounds(12, 65, 160, 15);
			fr.add(lblService);
			
			JLabel lblIp = new JLabel(Main.socket.getInetAddress() + ":" + Main.socket.getPort());
			lblIp.setFont(new Font("����", Font.PLAIN, 12));
			lblIp.setBounds(12, 105, 160, 15);
			fr.add(lblIp);
			
			JLabel lblAddress = new JLabel(Main.address);
			lblAddress.setFont(new Font("����", Font.PLAIN, 12));
			lblAddress.setBounds(12, 145, 160, 15);
			fr.add(lblAddress);
			
			JLabel lblServicedate = new JLabel(Main.servicedate.toString());
			lblServicedate.setFont(new Font("����", Font.PLAIN, 12));
			lblServicedate.setBounds(12, 186, 160, 15);
			fr.add(lblServicedate);
			
			fr.setResizable(false);
			fr.setVisible(true);
		}});
		
		database.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent event) {
			JFrame dataf = new JFrame("DataBase");
			dataf.addWindowListener(new WindowAdapter() {public void windowClosing(WindowEvent event) {
				dataf.setVisible(false);
				dataf.dispose();
			}});
			Dimension dim = new Dimension(500, 300);
			dataf.setPreferredSize(dim);
			
			JScrollPane sp = new JScrollPane(getTable(0, ""));
			
			Choice c = new Choice();
			c.add("[ �׸� ���� ]"); c.add("Ŭ���̾�Ʈ �̸�"); c.add("����"); c.add("������"); c.add("��ȭ��ȣ"); c.add("��¥");
			
			JTextField field = new JTextField();
			
			JButton find = new JButton("�˻�");
			find.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent event) {
				if(c.getSelectedIndex() == 0)
					return;
				int type = c.getSelectedIndex();
				if(c.getSelectedIndex() >= 4)
					type++;
				sp.setViewportView(getTable(type, field.getText()));
			}});
			
			JPanel p = new JPanel();
			p.setLayout(new GridLayout(1, 3));
			
			p.add(c);
			p.add(field);
			p.add(find);
			
			dataf.add(p, "North");
			dataf.add(sp, "Center");
			dataf.pack();
			
			dataf.setVisible(true);
			dataf.setResizable(false);
		}});
		
		client.add(clientB);
		client.add(database);
		
		m.add(fileSend);
		m.addSeparator();
		m.add(range);
		m.add(message);
		m.addSeparator();
		m.add(voiceChat);
		
		mb.add(m);
		mb.add(client);
		setJMenuBar(mb);
		
		addWindowListener(new WindowAdapter() {public void windowClosing(WindowEvent event) {System.exit(0);}});
		setLocation(0, 0);
		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		
		drawLocation = new DropTargetJLabel();
		drawLocation.setHorizontalAlignment(JLabel.CENTER);
		
		add(drawLocation, "Center");
		
		setResizable(false);
		setVisible(true);
	}
	
	public JTable getTable(int type, String filter) {
		String[] header = {"����", "����", "������", "�ּ�", "��ȭ��ȣ", "��¥"};
		String[][] contents = {};
		
		DefaultTableModel model = new DefaultTableModel(contents, header);
		JTable table = new JTable(model);
		table.setSize(500, 300);
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		
		try {
			Class.forName("org.mariadb.jdbc.Driver");
			conn = DriverManager.getConnection(Main.jdbcURL, Main.userID, Main.pwd);
			stmt = conn.createStatement();
			set = stmt.executeQuery("select * from astable");
			
			while(set.next()) {
				if(type != 0) {
					if(type == 6) {
						if(!(set.getDate(type).toString().contains(filter)) && !filter.equals(""))
							continue;
					} else if(type >= 1 && type <= 5) {
						if(!(set.getString(type).contains(filter)) && !filter.equals(""))
							continue;
					} else
						return null;
				}
				Vector<String> input = new Vector<String>();
				
				for(int i=1; i<=5; i++)
					input.add(set.getString(i));
				input.add(set.getDate(6).toString());
				
				model.addRow(input);
			}
			
			stmt.close();
			conn.close();
		} catch (ClassNotFoundException | SQLException exc) {
			exc.printStackTrace();
		}
		
		return table;
	}
	
	public static void drawImage(BufferedImage image) {
		Main.gui.setSize(image.getWidth(), image.getHeight());
		drawLocation.setIcon(new ImageIcon(image.getScaledInstance(drawLocation.getWidth(), drawLocation.getHeight(), Image.SCALE_SMOOTH)));
		
		clientSize = new Dimension(image.getWidth(), image.getHeight());
	}
	
	public class DropTargetJLabel extends JLabel implements DropTargetListener {
		
		public DropTargetJLabel() {
			new DropTarget(this, this); 
		}
		
		public void dragEnter(DropTargetDragEvent event) {}
		public void dragOver(DropTargetDragEvent event) {}
		public void dragExit(DropTargetEvent event) {}
		public void dropActionChanged(DropTargetDragEvent event) {}
		
		public void drop(DropTargetDropEvent event) {
			String filePath = null;
			
			try {
				DataFlavor fileList = DataFlavor.javaFileListFlavor;
				
				if(event.isDataFlavorSupported(fileList)) {
					event.acceptDrop(DnDConstants.ACTION_COPY);
					
					@SuppressWarnings("unchecked")
					List<File> list = (List<File>)event.getTransferable().getTransferData(fileList);
					filePath = list.get(0).getAbsolutePath();
				} else if(event.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					event.acceptDrop(DnDConstants.ACTION_COPY);
					filePath = (String)event.getTransferable().getTransferData(DataFlavor.stringFlavor);
					
					if(filePath.startsWith("file://"))
						filePath = filePath.substring(7);
					filePath = stripSuffix(stripSuffix(filePath, "\n"), "\r").replaceAll("%20", " ");
				}
				answer.send(new File(filePath));
			} catch (UnsupportedFlavorException | IOException e) {e.printStackTrace();}
		}
		
		private String stripSuffix(String s, String suffix) {
			return !s.endsWith(suffix) ? s : s.substring(0, s.length() - suffix.length());
		}
	}
}
