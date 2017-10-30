import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class connPane extends JPanel implements ActionListener
{
	JTabbedPane jtpane = new JTabbedPane();
	//JButton logout_Btn = new JButton("로그아웃");
	public connPane() {
		
		setLayout(new BorderLayout());
		jtpane.add("Master", new serverPane());
		jtpane.add("Slave", new clientPane());
		add("Center",jtpane);
		//add("North", logout_Btn);
		//logout_Btn.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		setVisible(false);
		removeAll();
		add(new loginPane());
		setVisible(true);
	}

}
