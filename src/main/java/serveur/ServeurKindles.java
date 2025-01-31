/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveur;

import dao.*;
import model.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ce Thread sera lancer au demarage du systeme pour assurer les
 * communicartions avec les kindles sans interompre les taches de gestion
 * qui seront relisees par l'admin
 */
public class ServeurKindles extends Thread {


    public void run() {
        try {
            ServerSocket sersoc = new ServerSocket(1000);
            while (true) {
                Socket soc = sersoc.accept();
                SocketThread st = new SocketThread(soc);
                Thread t = new Thread(st);
                t.start();
            }

        } catch (IOException | SQLException ex) {
            Logger.getLogger(ServeurKindles.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Classe interne
    class SocketThread implements Runnable {

        private Socket soc;
        final DocumentUtile docDAO = new DocumentUtile();
        final AdherentUtile adDAO = new AdherentUtile();
        final AuthenticationUtile authDAO = new AuthenticationUtile();
        final KindleUtile kindDAO = new KindleUtile();
        final EmpruntUtile empDAO = new EmpruntUtile();


        public SocketThread(Socket soc) throws SQLException {
            this.soc = soc;
        }

        public void run() {
            OutputStream streamOut = null;
            InputStream streamIn = null;

            try {
                streamOut = soc.getOutputStream();
                OutputStreamWriter sortie = new OutputStreamWriter(streamOut);
                streamIn = soc.getInputStream();
                BufferedReader entree = new BufferedReader(new InputStreamReader(streamIn));

                //enchainement a definir selon le protocole en se seravt de
                //String message = entree.readLine();
                //sortie.write(message) ;
                /**
                 * recevoir information d'authentification
                 * envoyer decision authentification
                 * attendre une requete de recherche de la part de l'adherent
                 * anvoyer le resultat de la requete
                 * repeter les deux taches precedents jusqu'a la deconnexion de l'adherent
                 * une fois que l'adherent est deconnecté alors return
                 **/
                boolean repeat = true;
                Abonne user = authentication(entree, sortie); // authentication
                Kindle k = getKindleInfos(entree, sortie);
                boolean assoc = associate(k, user);
                if(assoc == false){
                    sortie.write("L'utilisateur a deja un kindle!");
                    soc.close();
                }

                while (repeat) {

                        String test = entree.readLine();
                        switch (test) {
                            case "all" -> getAllDocs(sortie);
                            case "titre" -> getDocumentByTitre(entree, sortie);
                            case "editeur" -> getDocumentsByEditeur(entree, sortie);
                            case "edition" -> getDocumentsByEdition(entree, sortie);
                            case "auteur" -> getDocumentsByAuteur(entree, sortie);
                            case "quit" -> {
                                repeat = false;
                                empDAO.supprimerEmprunt(empDAO.getEmprunt(user, k));
                                soc.close();
                            }
                        }
                }

            } catch (IOException ex) {
//                Logger.getLogger(ServeurKindles.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    streamOut.close();
                    streamIn.close();
                    soc.close();
                } catch (IOException ex) {
                    Logger.getLogger(ServeurKindles.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        public Abonne authentication(BufferedReader entree, OutputStreamWriter sortie) throws IOException, SQLException {
            String username = entree.readLine();
            String password = entree.readLine();
            Abonne auth = authDAO.authentication(username, password);
            if (auth == null) sortie.write("unauthorized\n");
            else {
                sortie.write("succeded" + "\n");
            }
            sortie.flush();
            return auth;
        }

        public Kindle getKindleInfos(BufferedReader entree, OutputStreamWriter sortie) throws IOException, SQLException {
            /**
             * TODO: Get kindle infos from Kindle client through streams
             * Infos : code_kinle and mac
             * Create Kindle object then return it
             * Kindle.setEmprunter(true)
             */
            String code_kindle = entree.readLine();
            return kindDAO.getKindleByCode(code_kindle);
        }

        public boolean associate(Kindle k, Abonne u) throws SQLException {
            // TODO: Associate Kindle to abonne through Emprunt Object
            // Return true if added false if not
            return empDAO.emprunter(k, u);
        }

        public void getAllDocs(OutputStreamWriter sortie) throws IOException, SQLException {
            LinkedList doc = docDAO.getAllDocuments();
            StringBuilder response = new StringBuilder("");
            for (Object d : doc) {
                if (d instanceof Livre) {
                    Livre l = (Livre) d;
                    response.append(l.toString());
                    response.append("\n");
                } else {
                    Roman r = (Roman) d;
                    response.append(r.toString());
                    response.append("\n");
                }
            }
            sortie.write("--------All documents---------\n" + response + "\n");
            sortie.flush();
        }

        public void getDocumentByTitre(BufferedReader entree, OutputStreamWriter sortie) throws IOException, SQLException {
            String titre = entree.readLine();
            Document doc = docDAO.getDocumentByTitre(titre);
            if(doc == null){
                sortie.write("No document with this title found!\n");
                sortie.flush();
                return;
            }
            if (doc instanceof Livre) {
                Livre l = (Livre) doc;
                sortie.write(l.toString() + "\n");
                sortie.flush();
            } else if (doc instanceof Roman) {
                Roman r = (Roman) doc;
                sortie.write(r.toString() + "\n");
                sortie.flush();
            }

        }

        public void getDocumentsByEditeur(BufferedReader entree, OutputStreamWriter sortie) throws IOException, SQLException {
            String editeur = entree.readLine();
            LinkedList doc = docDAO.getDocumentsByEditeur(editeur);
            StringBuilder response = new StringBuilder("");

            for (Object d : doc) {
                if (d instanceof Livre) {
                    Livre l = (Livre) d;
                    response.append(l.toString());
                    response.append("\n");
                } else if (d instanceof Roman) {
                    Roman r = (Roman) d;
                    response.append(r.toString());
                    response.append("\n");
                }
            }
            sortie.write("Documents with editeur: " + editeur + "\n" + response + "\n");
            sortie.flush();
        }

        public void getDocumentsByEdition(BufferedReader entree, OutputStreamWriter sortie) throws IOException, SQLException {
            String edition = entree.readLine();
            LinkedList doc = docDAO.getDocumentsByEdition(edition);
            StringBuilder response = new StringBuilder("");

            for (Object d : doc) {
                if (d instanceof Livre) {
                    Livre l = (Livre) d;
                    response.append(l.toString());
                    response.append("\n");
                } else {
                    Roman r = (Roman) d;
                    response.append(r.toString());
                    response.append("\n");
                }
            }
            sortie.write("Documents with edition: " + edition + "\n" + response + "\n");
            sortie.flush();
        }

        public void getDocumentsByAuteur(BufferedReader entree, OutputStreamWriter sortie) throws IOException, SQLException {
            String auteur = entree.readLine();
            LinkedList doc = docDAO.getDocumentsByAuteur(auteur);
            StringBuilder response = new StringBuilder("");

            for (Object d : doc) {
                if (d instanceof Livre) {
                    Livre l = (Livre) d;
                    response.append(l.toString());
                    response.append("\n");
                } else {
                    Roman r = (Roman) d;
                    response.append(r.toString());
                    response.append("\n");
                }
            }
            sortie.write("Documents with auteur: " + auteur + "\n" + response + "\n");
            sortie.flush();
        }

    }

} 
