package nl.uu.cs.arg.platform.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AboutDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			AboutDialog dialog = new AboutDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public AboutDialog() {
		setBounds(100, 100, 450, 265);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel paneButtons = new JPanel();
			paneButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(paneButtons, BorderLayout.SOUTH);
			{
				JButton btnClose = new JButton("Close");
				btnClose.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						setVisible(false);
					}
				});
				btnClose.setActionCommand("OK");
				paneButtons.add(btnClose);
				getRootPane().setDefaultButton(btnClose);
			}
		}
		{
			JPanel paneAbout = new JPanel();
			getContentPane().add(paneAbout, BorderLayout.CENTER);
			{
				JLabel lblbaidddevelopedBy = new JLabel("<html><center>\r\n<p><h1>Baidd 0.1</h1><br /></p>\r\n\r\n<p>Developed by Eric Kok<br />\r\n<a href=\"mailto:erickok@cs.uu.nl\">erickok@cs.uu.nl</a><br />\r\n<a href=\"http://people.cs.uu.nl/erickok/\">people.cs.uu.nl/erickok</a><br /><br /></p>\r\n\r\n<p>Department of Information and Computing Sciences<br />\r\n<a href=\"http://www.cs.uu.nl\">www.cs.uu.nl</a><br />\r\nUniversity Utrecht</p>\r\n</center>\r\n</html>");
				paneAbout.add(lblbaidddevelopedBy);
			}
		}
	}

}
