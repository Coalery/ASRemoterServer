import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;

public class Main {
	public static final String jdbcURL = "jdbc:mariadb://127.0.0.1:3306/ASDB";
	public static final String userID = "root";
	public static final String pwd = "12345678";
	
	public static String name = "";
	public static String service = "";
	public static String address = "";
	public static String phonenum = "";
	public static Date servicedate;
	
	static Socket socket, scrSocket;
	static GUI gui;
	
	public void start() {
		int port = 5678;
		
		ServerSocket ss = null;
		ServerSocket ss2 = null;
		
		try {
			ss = new ServerSocket(port);
			ss2 = new ServerSocket(port+1);
			 
			socket = ss.accept();
			scrSocket = ss2.accept();
			
			System.out.println("[+] " + socket.getInetAddress() + ":" + socket.getPort() + ", " + socket.getInetAddress() + ":" + socket.getPort());
		} catch (IOException e) {e.printStackTrace();}
		
		servicedate = new Date();
		gui = new GUI();
		addDataAtDataBase(name, service, socket.getInetAddress(), phonenum, address);
	}
	
	public void addDataAtDataBase(String clientName, String service, InetAddress ip, String phonenum, String clientAddress) {
		Connection conn = null;
		Statement stmt = null;
		
		try {
			Class.forName("org.mariadb.jdbc.Driver");
			conn = DriverManager.getConnection(jdbcURL, userID, pwd);
			stmt = conn.createStatement();
			
			Calendar servicedate_cal = Calendar.getInstance();
			stmt.execute("insert into astable values ('" + clientName + "', '" + service + "', '" + ip.toString() + "', '" + clientAddress + "', '" + phonenum + "', '" + servicedate_cal.get(Calendar.YEAR) + "-" + servicedate_cal.get(Calendar.MONTH) + "-" + servicedate_cal.get(Calendar.DATE) + "')");
			
			stmt.close();
			conn.close();
		} catch (ClassNotFoundException | SQLException exc) {
			exc.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new Main().start();
	}
}
