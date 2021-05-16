package fr.ul.miage.projetreseau;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class connexionInterface {

    private JTextField login;
    private JTextField password;
    private JLabel loginLabel;
    private JLabel passwordLabel;
    private JButton validation;
	   
	public connexionInterface (File htpasswd) {
		
		JFrame fenetre = new JFrame();
		
		loginLabel.setText("login :");
		passwordLabel.setText("password :");
		validation.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
			try {
				// Créer l'objet File Reader
			    FileReader fr;
			    fr = new FileReader(htpasswd);
			    // Créer l'objet BufferedReader
			    BufferedReader br = new BufferedReader(fr);
			    StringBuffer sb = new StringBuffer();
			    String line;
			    
					while (((line = br.readLine()) != null)) {
					    //On teste si le login de l'utilisateur ainsi que son mdp correspondent
						if (line.split(":")[0].equals(login.getText()) 
						    && line.split(":")[1].equals(password.getText()) ) {
							
							//On accède à la page index.html
							
						    
						}
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		
		fenetre.add(login);
		fenetre.add(password);
		fenetre.add(loginLabel);
		fenetre.add(passwordLabel);
		fenetre.add(validation);
		
		fenetre.pack();
		fenetre.setVisible(true);
		
	}
	
	
}
