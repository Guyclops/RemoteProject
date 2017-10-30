import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class loginPane extends JPanel implements ActionListener{

	JPanel panel = new JPanel();
		JPanel center = new JPanel();
			JPanel center_c = new JPanel();	
			JTextField id_Tf = new JTextField();
			JPasswordField pwd_Tf = new JPasswordField();
			
			JPanel center_w = new JPanel();
			JLabel id_lbl = new JLabel("ID : ", JLabel.RIGHT);
			JLabel pwd_lbl = new JLabel("Password : ", JLabel.RIGHT);
			
			JButton login_Btn = new JButton("로그인");
			JButton join_Btn = new JButton("회원가입");
			
	jdbcConnect dbconn = new jdbcConnect();
	public loginPane() {
		setLayout(null);
		//setLayout(new BorderLayout());
		panel.setLayout(new BorderLayout());
		
		center.setLayout(new BorderLayout());
		center.add("East", login_Btn);
		
		center_c.setLayout(new GridLayout(0,1));
		center_c.add(id_Tf);
		center_c.add(pwd_Tf);
		
		center_w.setLayout(new GridLayout(0,1));
		center_w.add(id_lbl);
		center_w.add(pwd_lbl);
		
		center.add("Center",center_c);
		center.add("West",center_w);
		
		panel.add("South", join_Btn);
		panel.add("Center", center);
		
		
		//add("South",panel);
		setBackground(Color.white);
		setSize(400,500);
		LineBorder p_line = new LineBorder(Color.BLACK,5);
		LineBorder com_line = new LineBorder(Color.BLACK,1);
		id_lbl.setBorder(com_line);
		pwd_lbl.setBorder(com_line);
		login_Btn.setBorder(com_line);
		id_Tf.setBorder(com_line);
		pwd_Tf.setBorder(com_line);
		
		
		panel.setBounds(getWidth()/2-150, getHeight()/2-100, 300, 100);
		panel.setBorder(p_line);
		add(panel);
		login_Btn.addActionListener(this);
		
		join_Btn.addActionListener(this);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if(obj.equals(login_Btn)){
			checkMember();
			
		}else if(obj.equals(join_Btn)){
			switchjoinMember();
		}
	}
	
	
	public void checkMember(){
		try {
			String id = id_Tf.getText().trim();
			String pwd = pwd_Tf.getText().trim();
			if(id.equals("")||pwd.equals(""))
			{
				JOptionPane.showMessageDialog(null, "아이디 또는 비밀번호를 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
				return;
			}
			dbconn.conn = DriverManager.getConnection(dbconn.url,dbconn.user,dbconn.pwd);
		
			String sql = "select count(*) from users where users_id=? and users_pw=?";
			dbconn.pstmt = dbconn.conn.prepareStatement(sql);
			dbconn.pstmt.setString(1, id);
			dbconn.pstmt.setString(2, pwd);
		
			dbconn.resultset = dbconn.pstmt.executeQuery();
			while(dbconn.resultset.next()){
				int result = dbconn.resultset.getInt(1);
				if(result==1){
					dbconn.dbClose();
					switchConnPane();
					return;
				}else{
					JOptionPane.showMessageDialog(null, "아이디 또는 비밀번호가 틀렸습니다.", "경고", JOptionPane.WARNING_MESSAGE);
					dbconn.dbClose();
					return;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			dbconn.dbClose();
		}
	}
	
	public void switchConnPane(){
		setVisible(false);
		removeAll();
		setLayout(new BorderLayout());
		add("Center", new connPane());
		setVisible(true);
	}
	
	public void switchjoinMember()
	{
		setVisible(false);
		removeAll();
		setLayout(new BorderLayout());
		//add("Center", new joinPane());
		add(new joinPane());
		setVisible(true);
	}
}
