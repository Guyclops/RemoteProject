import java.awt.Toolkit;

import javax.swing.JFrame;

public class mainProject extends JFrame{

	final int w=Toolkit.getDefaultToolkit().getScreenSize().width; 
	final int h=Toolkit.getDefaultToolkit().getScreenSize().height;
	int x = w/2-200;
	int y = h/2-250;
	public mainProject() {
		
		add("Center",new connPane());
		//add("Center",new loginPane());
		setBounds(x,y,400,550);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
	}

	public static void main(String[] args) {
		new mainProject();
	}

}
