package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by priya on 4/4/15.
 */
public class NodeList implements Serializable{

    ArrayList<Node_Info> an = null;

    public void set_list(ArrayList<Node_Info> a){
        an = a;

    }

    public ArrayList<Node_Info> get_list(){
        return an;
    }


}
