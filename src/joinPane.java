import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.DriverManager;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class joinPane extends JPanel implements ActionListener{
	JPanel panel = new JPanel();
	JPanel center = new JPanel();
		JPanel center_c = new JPanel();	
		JTextField id_Tf = new JTextField();
		JPasswordField pwd_Tf = new JPasswordField();
		
		JPanel center_w = new JPanel();
		JLabel id_lbl = new JLabel("ID : ", JLabel.RIGHT);
		JLabel pwd_lbl = new JLabel("Password : ", JLabel.RIGHT);
		
		JPanel south = new JPanel(new GridLayout(0,1));
		JButton com_Btn = new JButton("가입");
		JButton can_Btn = new JButton("취소");
		
		jdbcConnect dbconn = new jdbcConnect();
	public joinPane() {
		setLayout(null);
		//setLayout(new BorderLayout());
		panel.setLayout(new BorderLayout());
		
		center.setLayout(new BorderLayout());
		
		
		center_c.setLayout(new GridLayout(0,1));
		center_c.add(id_Tf);
		center_c.add(pwd_Tf);
		
		center_w.setLayout(new GridLayout(0,1));
		center_w.add(id_lbl);
		center_w.add(pwd_lbl);
		
		center.add("Center",center_c);
		center.add("West",center_w);
		
		south.add(com_Btn);
		south.add(can_Btn);
		panel.add("South", south);
		panel.add("Center", center);
		
		
		
		setBackground(Color.white);
		setSize(400,500);
		LineBorder p_line = new LineBorder(Color.BLACK,5);
		LineBorder com_line = new LineBorder(Color.BLACK,1);
		id_lbl.setBorder(com_line);
		pwd_lbl.setBorder(com_line);
		id_Tf.setBorder(com_line);
		pwd_Tf.setBorder(com_line);
		
		
		panel.setBounds(getWidth()/2-150, getHeight()/2-100, 300, 150);
		//panel.setBounds(50, 150, 300, 150);
		//add("South",panel);
		add(panel);
		panel.setBorder(p_line);
		com_Btn.addActionListener(this);
		can_Btn.addActionListener(this);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if(obj.equals(com_Btn)){
			try{
				String id = id_Tf.getText().trim();
				String pwd = pwd_Tf.getText().trim();
				
				if(id.equals("")||pwd.equals("")){
					JOptionPane.showMessageDialog(null, "���̵�� ��й�ȣ�� �Է��ϼ���.", "���", JOptionPane.WARNING_MESSAGE);
					return;
				}
				dbconn.conn = DriverManager.getConnection(dbconn.url,dbconn.user,dbconn.pwd);
				String sql = "select count(users_id) from users where users_id=?";
				dbconn.pstmt = dbconn.conn.prepareStatement(sql);
				dbconn.pstmt.setString(1, id);
			
				dbconn.resultset = dbconn.pstmt.executeQuery();
				dbconn.resultset.next();
				int result = dbconn.resultset.getInt(1);
				if(result > 0){
					JOptionPane.showMessageDialog(null, "�ߺ��� ���̵� �Դϴ�.", "���", JOptionPane.WARNING_MESSAGE);
					dbconn.dbClose();
					return;
				}
			
				sql = "insert into users(users_id, users_password) "
						+"values(?,?)";
				dbconn.pstmt = dbconn.conn.prepareStatement(sql);
				dbconn.pstmt.setString(1, id);
				dbconn.pstmt.setString(2, pwd);
				int cnt = dbconn.pstmt.executeUpdate();
				if(cnt>0){
					dbconn.dbClose();
					/*setVisible(false);
					removeAll();
					add(new loginPane());
					setVisible(true);*/
					switchLoginPane();
					return;
				}else{
					dbconn.dbClose();
				}
			
			}catch(Exception se){
				dbconn.dbClose();
			}finally{
				dbconn.dbClose();
			}
		}else if(obj.equals(can_Btn)){
			switchLoginPane();
			return;
		}
	}
	
	public void switchLoginPane(){
		setVisible(false);
		removeAll();
		setLayout(new BorderLayout());
		add(new loginPane());
		setVisible(true);
	}

}
