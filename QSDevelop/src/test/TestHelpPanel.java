package test;

import pers.di.common.CTest;
import utils.QS1802.HelpPanel;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

public class TestHelpPanel {
	
	@CTest.test
	public static void test_HelpPanel()
	{
		HelpPanel cHelpPanel = new HelpPanel();
		cHelpPanel.start();
	}
	
	public static void main(String[] args) {
		CTest.ADD_TEST(TestHelpPanel.class);
		CTest.RUN_ALL_TESTS();
	}
}
