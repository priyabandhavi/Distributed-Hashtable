package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by priya on 4/2/15.
 */
public class Resultquery implements Serializable{

    HashMap<String,String> h = null;
    String toid;

    public void set_list( HashMap<String,String> s){
        h = s;
    }

    public HashMap<String,String>get_list(){
        return h;
    }


   public void set_toid(String to){
       toid = to;

   }

    public String get_toid(){
        return toid;
    }

}
