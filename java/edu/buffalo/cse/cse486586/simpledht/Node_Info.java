package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Created by priya on 4/1/15.
 */
public class Node_Info implements Comparable<Node_Info>,Serializable{

    String successor,predecessor,myidhash, myport = null;

    public void setsucc(String s){
        successor = s;
    }

    public String getsuc(){
        return successor;
    }


    public void setpre(String p){
        predecessor = p;
    }

    public String getpre(){
        return predecessor;
    }


    public void setHash(String node) throws NoSuchAlgorithmException {
        myidhash = genHash(node);
    }

    public String getHash(){
        return myidhash;
    }

    public void set_mynode(String n){
        myport = n;
    }

    public String get_mynode(){
        return myport;
    }

    private String genHash(String input) throws NoSuchAlgorithmException{
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for(byte b: sha1Hash){
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }







    @Override
    public int compareTo(Node_Info node_info) {
        return this.getHash().compareTo(node_info.getHash());
    }
}
