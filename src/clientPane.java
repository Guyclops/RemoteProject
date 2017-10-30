import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class clientPane extends JPanel implements ActionListener, Runnable {

	int w = Toolkit.getDefaultToolkit().getScreenSize().width, h = Toolkit.getDefaultToolkit().getScreenSize().height;
	JPanel north = new JPanel(new BorderLayout());
	JPanel north_c = new JPanel(new GridLayout(0, 1));
	JPanel north_1 = new JPanel(new BorderLayout());
	JLabel iplbl = new JLabel(String.format("%10s", "IP : "));
	JTextField ipTf = new JTextField();

	JPanel north_2 = new JPanel(new BorderLayout());
	JLabel portlbl = new JLabel("Port : ");
	JTextField portTf = new JTextField();

	JPanel north_e = new JPanel(new GridLayout(0,1));
	JButton connBtn = new JButton("연결");
	JButton disconnBtn = new JButton("해제");
	JTextArea serverTa = new JTextArea();
	JScrollPane sp = new JScrollPane(serverTa);
	JPanel south = new JPanel();
	JTextField sendTf = new JTextField();
	JButton sendBtn = new JButton("보내기");
	InetAddress ia;
	boolean connect = false;

	ServerSocket server;
	DatagramSocket ds;
	DatagramPacket dp;
	Socket socket;
	int port;
	BufferedImage image;
	Robot r;
	DataOutputStream dos;

	MsgThread msgThread;
	MouseThread mouseThread;
	double multipleX, multipleY;
	public clientPane() {
		setLayout(new BorderLayout());
		add("North", north);
		north.add("Center", north_c);
		north_1.add("West", iplbl);
		north_1.add("Center", ipTf);
		north_c.add(north_1);
		north_2.add("West", portlbl);
		north_2.add("Center", portTf);
		north_c.add(north_2);
		north_e.add(connBtn);
		north_e.add(disconnBtn);
		north.add("East", north_e);

		settingComponent(false);

		add("Center", sp);
		add("South", south);
		south.setLayout(new BorderLayout());
		south.add("Center", sendTf);
		south.add("East", sendBtn);
		ipTf.setEnabled(false);

		connBtn.addActionListener(this);
		sendBtn.addActionListener(this);
		sendTf.addActionListener(this);
		disconnBtn.addActionListener(this);
		try {
			ia = InetAddress.getLocalHost();
			ipTf.setText(ia.getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void settingComponent(boolean check){
		serverTa.setEditable(false);
		sendTf.setEnabled(check);
		sendBtn.setEnabled(check);
		disconnBtn.setEnabled(check);
		connBtn.setEnabled(!check);
		portTf.setEnabled(!check);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj.equals(connBtn)) {
			client_work();
			connect = true;
		}else if(obj.equals(sendBtn)||obj.equals(sendTf)){
			sendMessage();
		}else if(obj.equals(disconnBtn)){
			clientClose();
		}
	}
	
	public void sendMessage(){
		try{
			String str = sendTf.getText().trim();
			if(str.equals(""))return;
			sendTf.setText("");
			serverTa.append("[Slave] "+str+"\n");
			serverTa.setCaretPosition(serverTa.getText().length());
			DatagramPacket sendPacket = new DatagramPacket(str.getBytes(), str.getBytes().length,
				socket.getInetAddress(),socket.getPort());
			ds.send(sendPacket);
		}catch(SocketException e){
			clientClose();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void client_work() {
		port = Integer.parseInt(portTf.getText());
		try {
			new Thread(this).start();
			new screen().start();
		} catch (Exception e) {
			// ���� �ƿ�ǲ
			clientClose();
			e.getMessage();
		}
	}

	class screen extends Thread{
		public void run() {
			try {
				//clientClose();
				settingComponent(false);
				server = new ServerSocket(port);
				serverTa.setText("연결 대기중.\n");
				
				disconnBtn.setEnabled(true);
				connBtn.setEnabled(false);
				portTf.setEnabled(false);
				
				socket = server.accept();
				ds = new DatagramSocket(port);
				serverTa.append("Master와 연결되었습니다.\n");
				mouseThread = new MouseThread();
				mouseThread.start();
				msgThread = new MsgThread();
				msgThread.start();
				
				settingComponent(true);
				
				r = new Robot();
				/*OutputStream os = socket.getOutputStream();
				dos = new DataOutputStream(os);*/
				OutputStream os = socket.getOutputStream();
				dos = new DataOutputStream(os);
				while (true) {
					image = r.createScreenCapture(new Rectangle(0, 0, w, h));
					ByteArrayOutputStream arr = new ByteArrayOutputStream();
					ImageIO.write(image, "jpg", arr);
					arr.flush();
					byte[] imageInbyte = arr.toByteArray();
					arr.close();
					dos.writeInt(imageInbyte.length);
					dos.write(imageInbyte);
				}
			}catch(SocketException e){
				clientClose();
				e.getMessage();
			}catch (Exception e) {
				clientClose();
				e.printStackTrace();
			}
		}
	}
	@Override
	public void run() {
		
	}
	public void clientClose(){
		try {
			if(socket!=null){
				socket.close();
				socket = null;
			}
			if(ds!=null){
				ds.close();
				ds = null;
			}
			if(server!=null){
				server.close();
				server = null;
			}
			settingComponent(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	class MsgThread extends Thread{
		InputStream is;
		InputStreamReader isr;
		BufferedReader br;
		public MsgThread(){
			try{
				is = socket.getInputStream();
				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);
			}catch(Exception e){
				clientClose();
			}
		}
		public void run(){
			try{
				while(true){
					String str = br.readLine().trim();
					serverTa.append("[Master] "+str+"\n");
					serverTa.setCaretPosition(serverTa.getText().length());
				}
			}catch(Exception e){
				try {
					br.close();
					isr.close();
					is.close();
					clientClose();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	class MouseThread extends Thread{
		 InetAddress ia;
		 byte[] data;
		 Robot robot;
		 public MouseThread()
		 {
			 try {
				 
				 data = new byte[1024];
				 dp = new DatagramPacket(data, data.length);
				 robot = new Robot();
		 		 ds.receive(dp); 
		 		 String s = new String(data).trim();
		 		 System.out.println(s);
		 		 multipleX = (double)w/Integer.parseInt(s.substring(0, s.indexOf(",")));
		 		 multipleY = (double)h/Integer.parseInt(s.substring(s.indexOf(",")+1,s.length()));
		 		 System.out.println(multipleX+","+multipleY);
			} catch (SocketException e) {
				clientClose();
				e.printStackTrace();
			} catch (UnknownHostException e) {
				clientClose();
				e.printStackTrace();
			} catch (IOException e) {
				clientClose();
				e.printStackTrace();
			} catch (AWTException e) {
				e.printStackTrace();
			} 
		 }
		 public void run(){
			 while(true){
				 try {
					 byte[] data = new byte[1024];
					 dp = new DatagramPacket(data, data.length);
			 		 ds.receive(dp);
			 		 String s = new String(data).trim();
			 		 if(s.equals("[mousePL]")){
			 			 Point p = receiveMouse();
				 		robot.mouseMove((int)p.getX(),(int)p.getY());
				 		robot.mousePress(InputEvent.BUTTON1_MASK);
			 		 }else if(s.equals("[mousePW]")){
			 			 Point p = receiveMouse();
			 			robot.mouseMove((int)p.getX(),(int)p.getY());
				 		robot.mousePress(InputEvent.BUTTON2_MASK);
			 		 }else if(s.equals("[mousePR]")){
			 			 Point p = receiveMouse();
			 			robot.mouseMove((int)p.getX(),(int)p.getY());
				 		robot.mouseRelease(InputEvent.BUTTON2_MASK);
			 		 }	 else if(s.equals("[mouseM]")){
			 			 Point p = receiveMouse();
			 			 robot.mouseMove((int)p.getX(), (int)p.getY());
			 		 }else if(s.equals("[mouseRL]")){
			 			 receiveMouse();
				 		 robot.mouseRelease(InputEvent.BUTTON1_MASK);
			 		 }else if(s.equals("[mouseRR]")){
			 			 Point p = receiveMouse();
			 			 robot.mouseMove((int)p.getX(),(int)p.getY());
				 		 robot.mouseRelease(InputEvent.BUTTON3_MASK);
			 		 }else if(s.equals("[mouseWU]")){
			 			 receiveMouse();
			 			 robot.mouseWheel(-10);
			 		 }else if(s.equals("[mouseWD]")){
			 			 receiveMouse();
			 			robot.mouseWheel(10);
			 		 }else if(s.equals("[keyR]")){
			 			 int key = receiveKey();
			 			 robot.keyPress(key);
			 			 robot.keyRelease(key);
			 		 }
			 
			}catch(SocketException e){
				clientClose();
			}catch (IOException e) {
				clientClose();
				e.printStackTrace();
			}
			}
		 }
		 public Point receiveMouse(){
			 Point point = new Point();
			 try{
				data = new byte[1024];
	 			dp = new DatagramPacket(data, data.length);
	 			ds.receive(dp);
	 			String s = new String(data).trim();
	 			multipleX = (double)w/Integer.parseInt(s.substring(0, s.indexOf(",")));
		 		multipleY = (double)h/Integer.parseInt(s.substring(s.indexOf(",")+1,s.length()));
		 		data = new byte[1024];
	 			dp = new DatagramPacket(data, data.length);
	 			ds.receive(dp);
		 		s = new String(data).trim();
		 		int x = (int)(Integer.parseInt(s.substring(0, s.indexOf(",")))*multipleX);
		 		int y = (int)(Integer.parseInt(s.substring(s.indexOf(",")+1,s.length()))*multipleY);
		 		point.setLocation(x, y);
			 }catch(Exception e){
				 clientClose();
				 e.printStackTrace();
			 }
			 return point;
		 }
		 public int receiveKey(){
			 try{
				 	data = new byte[1024];
		 			dp = new DatagramPacket(data, data.length);
		 			ds.receive(dp);
			 		String s = new String(data).trim();
			 		return Integer.parseInt(s);
				 }catch(Exception e){
					 clientClose();
				 }
			 return 0;
		 }
	 }
}
