import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class serverPane extends JPanel implements ActionListener, Runnable{

	int w = 1280, h = 720;
	
	JPanel north = new JPanel(new BorderLayout());
		JPanel north_c = new JPanel(new GridLayout(0, 1));
			JPanel north_1 = new JPanel(new BorderLayout());
			JLabel iplbl = new JLabel(String.format("%10s", "IP : "));
			JTextField ipTf = new JTextField();
			JButton showsc = new JButton("영상보기");
			JButton logout_Btn = new JButton("로그아웃");
			
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
		
	showSlave show;
	Socket socket;
	InputStream is;
	DataInputStream dis;
	
	DatagramSocket ds;
	String serverip;
	
	int port;
	boolean count=true;
	boolean start=false;
	InetAddress ia;
	screenThread screen;
	public serverPane() {
		setLayout(new BorderLayout());
		add("North", north);
			north.add("Center", north_c);
				north_1.add("West", iplbl);
				north_1.add("Center", ipTf);
				
			north_c.add(north_1);
				north_2.add("West", portlbl);
				north_2.add("Center", portTf);
			north_c.add(north_2);
			north.add("South",showsc);
			north_e.add(connBtn);
			north_e.add(disconnBtn);
		north.add("East",north_e);
		//north.add("North", logout_Btn);
		settingComponent(false);
		
		add("Center",sp);
		add("South", south);
		south.setLayout(new BorderLayout());
		south.add("Center", sendTf);
		south.add("East",sendBtn);
		
		connBtn.addActionListener(this);
		showsc.addActionListener(this);
		disconnBtn.addActionListener(this);
		sendBtn.addActionListener(this);
		sendTf.addActionListener(this);
	}

	public void settingComponent(boolean check){
		serverTa.setEditable(false);
		sendTf.setEnabled(check);
		sendBtn.setEnabled(check);
		disconnBtn.setEnabled(check);
		showsc.setEnabled(check);
		connBtn.setEnabled(!check);
		ipTf.setEnabled(!check);
		portTf.setEnabled(!check);
	}
	
	public void initSocket(){
		dis = null;
		is = null;
		socket = null;
		ds = null;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if(obj.equals(sendTf)||obj.equals(sendBtn)){
			sendMessage();
		}else if(obj.equals(connBtn)){
			receiveScreen();
		}else if(obj.equals(showsc)){
			show.setVisible(count);
			count=!count;
		}else if(obj.equals(disconnBtn)){
			serverClose();
		}else if(obj.equals(logout_Btn)){
			serverClose();
		}
	}
	public void serverClose(){
		try {
			
			if(screen!=null){
				screen.stop();
				screen = null;
			}
			if(socket!=null){
				socket.close();
				socket=null;
			}
			if(ds!=null){
				ds.close();
				ds=null;
			}
			settingComponent(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public void sendMessage(){
		try{
			OutputStream os = socket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			PrintWriter pw = new PrintWriter(osw);
			pw.println(sendTf.getText());
			serverTa.append("[Master] "+sendTf.getText()+"\n");
			serverTa.setCaretPosition(serverTa.getText().length());
			sendTf.setText("");
			pw.flush();
		}catch(SocketException e){
			serverClose();
			e.getMessage();
		}catch(Exception e){
			serverClose();
			e.getMessage();
		}
	}
	public void receiveScreen(){
		serverip = ipTf.getText();
		port = Integer.parseInt(portTf.getText());
		try {
			initSocket();
			socket = new Socket(serverip, port);
			ds = new DatagramSocket();
			ia = InetAddress.getByName(socket.getInetAddress().getHostAddress());
			show = new showSlave();
			show.panel.addMouseListener(new MyMouse());
			show.panel.addMouseMotionListener(new MyMouse());
			show.panel.addMouseWheelListener(new MyMouse());
			show.addKeyListener(new MyKeyBoard());
			start=true;
			
			settingComponent(true);
			
			serverTa.setText("Slave와 연결되었습니다.\n");
			
			is = socket.getInputStream();
			dis = new DataInputStream(is);
			String s = w + "," + h;
			DatagramPacket dp = new DatagramPacket(s.getBytes(), s.getBytes().length, ia, port);
			ds.send(dp);
			new Thread(this).start();
			new MsgThread().start();
			screen = new screenThread();
			screen.start();
		} catch (UnknownHostException e) {
			//���� ����
			JOptionPane.showMessageDialog(null, "�߸��� hostname", "���", JOptionPane.WARNING_MESSAGE);
			serverClose();
			e.getMessage();
			//e.printStackTrace();
		} catch(SocketException e){
			JOptionPane.showMessageDialog(null, "�߸��� ip, port�Դϴ�.", "���", JOptionPane.WARNING_MESSAGE);
			serverClose();
			e.getMessage();
		}catch (IOException e) {
			// ����
			e.getMessage();
			//e.printStackTrace();
		}catch(Exception e){
			serverClose();
			e.getMessage();
		}
	}

	@Override
	public void run() {
		/*while(true){
			try{
				int len = dis.readInt();
				byte[] data = new byte[len];
				dis.readFully(data);
				ByteArrayInputStream bais = new ByteArrayInputStream(data);
				BufferedImage buffer = ImageIO.read(bais);
				show.panel.getGraphics().drawImage(buffer, 0,0,show.panel.getWidth(),show.panel.getHeight(),show.panel);
			}catch(SocketException e){
				serverClose();
			}catch(IOException e){
				serverClose();
			}catch(Exception e){
			}
		}
		*/
	}
	class screenThread extends Thread{
		public void run() {
			while(true){
				try{
					int len = dis.readInt();
					byte[] data = new byte[len];
					dis.readFully(data);
					ByteArrayInputStream bais = new ByteArrayInputStream(data);
					BufferedImage buffer = ImageIO.read(bais);
					show.panel.getGraphics().drawImage(buffer, 0,0,show.panel.getWidth(),show.panel.getHeight(),show.panel);
				}catch(SocketException e){
					serverClose();
					e.getMessage();
				}catch(IOException e){
					serverClose();
					e.getMessage();
				}catch(Exception e){
					//serverClose();
					e.getMessage();
				}
			}
			
		}
	}
	class MsgThread extends Thread{
		public void run(){
			try{
				DatagramSocket d = new DatagramSocket(socket.getLocalPort());
				while(true){
					byte[] data = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(data, data.length);
					d.receive(receivePacket);
					String msg = new String(receivePacket.getData()).trim();
					serverTa.append("[Slave] "+msg+"\n");
					serverTa.setCaretPosition(serverTa.getText().length());
				}
			}catch(Exception e){
				serverClose();
				e.printStackTrace();
			}
		}
	}
	class MyMouse extends MouseAdapter{
		
		public void sendingPoint(String str, int x, int y){
			try {
			String s = str;
			DatagramPacket dp = new DatagramPacket(s.getBytes(), s.getBytes().length, ia, port);
			ds.send(dp);
			s = show.panel.getWidth() + "," + show.panel.getHeight();
			dp = new DatagramPacket(s.getBytes(), s.getBytes().length, ia, port);
			ds.send(dp);
			s = x + "," + y;
			dp = new DatagramPacket(s.getBytes(), s.getBytes().length, ia, port);
			ds.send(dp);
			}catch(SocketException e){
				serverClose();
				e.getMessage();
			}catch (IOException e) {
				serverClose();
				e.printStackTrace();
			}
		}
		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			if(e.getButton()==MouseEvent.BUTTON1)
				sendingPoint("[mousePL]",e.getX(),e.getY());
			else if(e.getButton()==MouseEvent.BUTTON2)
				sendingPoint("[mousePW]",e.getX(),e.getY());
			else if(e.getButton()==MouseEvent.BUTTON3)
				sendingPoint("[mousePR]",e.getX(),e.getY());
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if(start){
				super.mouseMoved(e);
				sendingPoint("[mouseM]",e.getX(),e.getY());
			}
		}
		@Override
		public void mouseReleased(MouseEvent e){
			super.mouseDragged(e);
			if(e.getButton()==MouseEvent.BUTTON1)
				sendingPoint("[mouseRL]",e.getX(),e.getY());
			else if(e.getButton()==MouseEvent.BUTTON3)
				sendingPoint("[mouseRR]",e.getX(),e.getY());
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			super.mouseDragged(e);
			sendingPoint("[mouseM]",e.getX(),e.getY());
		}
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			super.mouseWheelMoved(e);
			int und = e.getWheelRotation();
			if(und<0){
				sendingPoint("[mouseWU]",e.getX(),e.getY());
			}else{
				sendingPoint("[mouseWD]",e.getX(),e.getY());
			}
		}
	}
	class MyKeyBoard extends KeyAdapter{
		public void sendingKey(String s, int keycode){
			try{
				String str = s;
				DatagramPacket dp = new DatagramPacket(str.getBytes(), str.getBytes().length, ia, port);
				ds.send(dp);
				s = keycode+"";
				dp = new DatagramPacket(s.getBytes(), s.getBytes().length, ia, port);
				ds.send(dp);
			}catch(SocketException e){
				serverClose();
				e.getMessage();
			}catch(IOException e){
				serverClose();
				e.getMessage();
			}
		}
		@Override
		public void keyReleased(KeyEvent e) {
			super.keyReleased(e);
			if(start){
				sendingKey("[keyR]",e.getKeyCode());
			}
		}
	}

}
