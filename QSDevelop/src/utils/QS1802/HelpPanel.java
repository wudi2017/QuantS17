package utils.QS1802;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class HelpPanel {

	public static class MainFramePanel extends JPanel
	{
		public MainFramePanel()
		{
			
		}
	}
	static class WindowListener extends WindowAdapter {  
		public void windowClosing(WindowEvent e) {  
			System.exit(0);  
		}  
	}  
	
	public void start()
	{
		JFrame jfrm = new JFrame();
		jfrm.setTitle("HelpPanel");
		jfrm.setSize(815, 600);
		jfrm.setContentPane(new MainFramePanel());
		jfrm.addWindowListener(new WindowListener());
		jfrm.setVisible(true);
	}
}
