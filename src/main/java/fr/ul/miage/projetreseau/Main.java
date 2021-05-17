package fr.ul.miage.projetreseau;

import java.io.File;
import java.io.IOException;

import fr.ul.miage.projetreseau.service.GeneralProperties;

public class Main {

	public static void main(String[] args) {
		try {
			// Site 1
			String serv1_domain = GeneralProperties.getPropertie("SERVERS[0].domain");
			String serv1_root = GeneralProperties.getPropertie("SERVERS[0].root");
			String serv1_port = GeneralProperties.getPropertie("SERVERS[0].port");
			new HTTPServer(new File(serv1_root), Integer.parseInt(serv1_port), serv1_domain);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * // Site 2 String serv2_domain =
		 * GeneralProperties.getPropertie("SERVERS[1].domain"); String serv2_root =
		 * GeneralProperties.getPropertie("SERVERS[1].root"); String serv2_port =
		 * GeneralProperties.getPropertie("SERVERS[1].port"); try { new HTTPServer(new
		 * File(serv2_root), Integer.parseInt(serv2_port), serv2_domain); } catch
		 * (IOException e) { e.printStackTrace(); }
		 */
	}

}
