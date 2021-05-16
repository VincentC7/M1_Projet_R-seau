package fr.ul.miage.projetreseau;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class ConnexionInterface {

    public boolean enable = false;

    public JFrame connexion(File htpasswd) {
	JFrame fenetre = new JFrame();
	JTextField login = new JTextField();
	JTextField password = new JTextField();
	JLabel loginLabel = new JLabel("login :");
	JLabel passwordLabel = new JLabel("password :");
	JButton validation = new JButton("Valider");
	validation.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
	    	
		try {
		    // Créer l'objet File Reader
		    FileReader fr;
		    fr = new FileReader(htpasswd);
		    // Créer l'objet BufferedReader
		    BufferedReader br = new BufferedReader(fr);
		    String line;
		    while (((line = br.readLine()) != null) && !enable) {
			// On teste si le login de l'utilisateur ainsi que son mdp correspondent
			if (line.split(":")[0].equals(login.getText())
				&& line.split(":")[1].equals(password.getText())) {
			    enable = true;
			}
		    }
		    br.close();
		    fenetre.setVisible(false);
		    fenetre.dispose(); //Quitte
		    
		}
		catch (IOException e1) {
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		}
	    }
	});
	login.setPreferredSize(new Dimension(100, 40));
	password.setPreferredSize(new Dimension(100, 40));
	//fenetre.setSize(500, 500);
	
	fenetre.setLayout(new FlowLayout());
	fenetre.add(loginLabel);
	fenetre.add(login);
	fenetre.add(passwordLabel);
	fenetre.add(password);
	fenetre.add(validation);
	fenetre.pack();
	fenetre.setVisible(true);
	fenetre.setFocusable(true);
	return fenetre;
    }

    public boolean isEnable() {
    	return enable;
    }

}
