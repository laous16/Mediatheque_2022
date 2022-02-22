/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import model.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.LinkedList;

/**
 *
 * @author rachad
 */
public class EmpruntUtile {
    private final Connection con;


    public  EmpruntUtile() throws SQLException {

        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mediatheque","root","");

    }

    public boolean ajouterEmprunt(Emprunt ep) throws SQLException {
        String query = "INSERT INTO emprunt (code_abonne,code_kindle) VALUES (?,?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, ep.getCode_abonne());
        ps.setString(2, ep.getCode_kindle());
        int rs = ps.executeUpdate(query);
        return rs > 0;
    }

    public boolean supprimerEmprunt(Emprunt ep) throws SQLException {
        Statement stmt = con.createStatement();
        String query="DELETE FROM emprunt WHERE code_kindle LIKE '"+ep.getCode_kindle()+"' AND code_abonne LIKE '"+ep.getCode_abonne()+"'";

        int nbUpdated = stmt.executeUpdate(query);
        return nbUpdated>0;
    }

    public Emprunt getEmprunt(Abonne ab , Kindle kd) throws SQLException {
        Emprunt ep=null;
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select * from emprunt where code_abonne like '"+ ab.getCode_abonne()+"' AND code_kindle LIKE '"+ep.getCode_kindle()+"'");
        while(rs.next()){
            ep=new Emprunt(rs.getString("code_abonne"), rs.getString("code_kindle"));

        }
        return ep;
    }

    public LinkedList<Emprunt> getEmpruntsByAbonne(Abonne ab) throws SQLException {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select * from emprunt where code_abonne like '"+ ab.getCode_abonne()+"'");

        LinkedList<Emprunt> emprunts= new LinkedList<>();

        while (rs.next()) {
            Emprunt ep= new Emprunt(rs.getString("code_abonne") ,rs.getString("code_kindle"));
            emprunts.add(ep);
        }
        return emprunts;
    }

    public LinkedList<Emprunt> getEmpruntsByKindle(Kindle kd) throws SQLException {

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select * from emprunt where code_kindle like '"+ kd.getCode_kindle()+"'");

        LinkedList<Emprunt> emprunts= new LinkedList<>();

        while (rs.next()) {
            Emprunt ep= new Emprunt(rs.getString("code_abonne") ,rs.getString("code_kindle"));
            emprunts.add(ep);
        }
        return emprunts;
    }

    public LinkedList<Emprunt> getAllEmprunts() throws SQLException {

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select * from emprunt");

        LinkedList<Emprunt> emprunts= new LinkedList<>();

        while (rs.next()) {
            Emprunt ep= new Emprunt(rs.getString("code_abonne") ,rs.getString("code_kindle"));
            emprunts.add(ep);
        }
        return emprunts;
    }
    
}
