import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
public class showSlave extends JFrame
{
	JPanel panel = new JPanel();
	public showSlave() {
		super("slave화면");
		add("Center", panel);
		setSize(1300, 760);
		//setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
}
