import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

public class Answer extends Thread {
	public static final int MOUSE_MOVE = 0;
	public static final int MOUSE_CLICK = 1;
	public static final int MOUSE_RELEASE = 2;
	public static final int MOUSE_WHEEL = 3;
	
	public static final int KEY_PRESS = 4;
	public static final int KEY_RELEASE = 5;
	
	public static final int TEXT_RECEIVE = 6;
	public static final int SET_CAPTURE_SIZE = 7;
	public static final int SET_FULL_SCREEN = 8;
	public static final int FILE_RECEIVE = 9;
	public static final int END = 10;
	
	public static final int START_VOICECALL = 11;
	
		private Socket scrSocket;
		private DataOutputStream out;
		private DataInputStream in;
		
		public Answer() {
			this.scrSocket = Main.scrSocket;
			
			String[] serviceStr = {"컴퓨터 최적화", "바이러스 치료", "프로그램 고장 지원", "기타"};
			
			try {
				out = new DataOutputStream(Main.socket.getOutputStream());
				in = new DataInputStream(Main.socket.getInputStream());
				
				Main.name = in.readUTF();
				int serviceIndex = in.read();
				Main.service = serviceStr[serviceIndex - 1];
				Main.address = in.readUTF();
				Main.phonenum = in.readUTF();
				
				System.out.println(Main.name);
			} catch (IOException e) {e.printStackTrace();}
		}
		
		@Override
		public void run() {
			while(true) {
				BufferedImage bimage = null;
				try {
					bimage = ImageIO.read(scrSocket.getInputStream());
				} catch (IOException e) {
				} catch (NullPointerException e) {break;}
				
				if(bimage == null)
					continue;
				
				GUI.drawImage(bimage);
			}
		}
		
		public void send(Point p) {
			try {
			out.write(MOUSE_MOVE);
			
			out.writeInt(((int)((double)(p.getX()) * (GUI.clientSize.getWidth() / GUI.drawLocation.getWidth()))) + GUI.addX);
			out.writeInt(((int)((double)(p.getY()) * (GUI.clientSize.getHeight() / GUI.drawLocation.getHeight()))) + GUI.addY);
			} catch (IOException e) {}
		}
		
		public void send(Point p, int mask, boolean released) {
			try {
			out.write(released ? MOUSE_RELEASE : MOUSE_CLICK);
			out.writeInt(mask);
			
			} catch (IOException e) {e.printStackTrace();}
		}
		
		public void send(int keycode, boolean isReleased) {
			try {
			out.write((isReleased) ? KEY_RELEASE : KEY_PRESS);
			out.writeInt(keycode);
			} catch (IOException e) {}
		}
		
		public void send(boolean updown) {
			try {
				out.write(MOUSE_WHEEL);
				out.writeInt((updown) ? 1 : -1);
			} catch (IOException e) {}
		}
		
		public void send(String message) {
			try {
				out.write(TEXT_RECEIVE);
				out.writeUTF(message);
			} catch (IOException e) {}
		}
		
		public void send(int x, int y, int width, int height) {
			try {
				out.write(SET_CAPTURE_SIZE);
				out.writeInt(x);
				out.writeInt(y);
				out.writeInt(width);
				out.writeInt(height);
			} catch (IOException e) {}
		}
		
		public void sendSingleCommand(int c) {
			try {
				out.write(c);
			} catch (IOException e) {}
		}
		
		public void send(File f) {
			try {
				out.write(FILE_RECEIVE);
				out.writeUTF(f.getName());
				out.writeLong(f.length());
				
				FileInputStream fis = new FileInputStream(f);
				byte[] buffer = new byte[4096];
				
				while(fis.read(buffer) > 0)
					out.write(buffer);
				
				fis.close();
			} catch (IOException e) {}
		}
	}