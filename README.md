
#Projet de réseau M1

Ce projet à pour objectif de recréer un serveur web HTTP

##Configuration

###Projet
Dans le répertoire Ressources_exte figure l'ensemble des ressources que va utiliser notre serveur. Site1 et Site2 sont deux ressources que nous vous fournissons pour effectuer vos tests. 
Si vous souhaitez définir vos sites il faut faire une **copie** de **conf_exemple.properties** nommée "**conf.properties**" modifier les infos existantes (domaine, port et répertoire racine du projet).   
Par exemple, si vous souhaitez créer un site toto, il vous suffit de modifier l'un des deux sites présents dans conf.properties et remplacer le "domaine" par www.toto.fr et le fichier "root" en Ressources_exte/mon_site_toto. 
Si vous le souhaitez, vous pouvez également attribuer un autre port à ce site (vérifiez qu'il n'est pas utilisé)

###Hosts
Pour que vos noms de domaines soient reconnus en local il faut modifier votre fichier hosts (voir google comment faire).  
Pour chaque domaine aujouter :
  * 127.0.0.1 www.exemple.fr 


##Lancement
Pour lancer le projet, vous avez plusieurs options :  
 * La première étant de lancer le main manuellement depuis un terminal.
 * La seconde est d'utiliser le .jar présent à la racine du projet via la commande **java -jar projetreseau-1.0.0-RELEASE-jar-with-dependencies.jar**

Actuellement, le projet contient deux sites. Le premier est protégé par un fichier htpasswd (login:tata ;mdp:toto). Le second n'est pas protégé.

##Maintenance
Le projet peut gérer que deux sites pour le moment. Si vous voulez ajouter un nouveau site, il faut ajoute un bloc dans la classe main, voici un exemple :  
```
try {
    String serv_domain = GeneralProperties.getPropertie("SERVERS[2].domain");
    String serv_root = GeneralProperties.getPropertie("SERVERS[2].root");
    String serv_port = GeneralProperties.getPropertie("SERVERS[].port");
    new HTTPServer(new File(serv_root), Integer.parseInt(serv_port), serv_domain);
} catch (IOException e) {
    e.printStackTrace();
}
```
