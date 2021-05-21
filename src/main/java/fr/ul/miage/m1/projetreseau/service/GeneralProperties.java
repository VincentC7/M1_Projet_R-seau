package fr.ul.miage.m1.projetreseau.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GeneralProperties {

    private static  Properties prop;

    /**
     * Permet de charger le fichier de paramétrage de l'application
     */
    private static void load(){
        try (InputStream input = new FileInputStream("Ressources_exte/conf.properties")) {
            prop = new Properties();
            prop.load(input);
        } catch (IOException ex) {
            System.err.println("Fichier de configuration introuvable");
        }
    }

    /**
     *  Retourne la valeur qui correspond au nom du paramètre de configuration que l'on passe en paramètre de la fonction
     *
     * @param propertieName Nom du paramètre auquel on souhaite accéder
     * @return
     */
    public static String getPropertie(String propertieName){
        load();
        return prop.getProperty(propertieName);
    }


}
