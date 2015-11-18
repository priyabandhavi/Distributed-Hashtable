package edu.buffalo.cse.cse486586.simpledht;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by priya on 3/30/15.
 */
public class Client extends AsyncTask<Object,Void,Void> {
    final String TAG = SimpleDhtActivity.class.getSimpleName();
    String[] Remote_ports = {"11112","11116","11120","11124"};

    @Override
    protected Void doInBackground(Object... objects) {

        Integer remote_port = null;
        String array[];
        Object msg = objects[0];


        try {
            if(msg instanceof String){
                array = objects[0].toString().split(";");
                if(array[0].contains("join")){
                    remote_port = Integer.parseInt(array[2])*2;
                    Log.d(TAG,"check join message");
                    sendMessage(objects[0],remote_port);
                }
                else if(array[0].contains("update")||array[0].equals("insert")||array[0].equals("query")||array[0].equals("query1")||array[0].equals("single_query")){
                    remote_port = Integer.parseInt(array[1])*2;
                    sendMessage(objects[0],remote_port);
                }


            }


            else if (msg instanceof Resultquery){
                Resultquery rq = (Resultquery)msg;
                remote_port = Integer.parseInt(rq.get_toid())*2;
                sendMessage(objects[0],remote_port);
            }

            else if (msg instanceof NodeList){
                NodeList l = (NodeList)msg;
                for(int i = 0; i < Remote_ports.length;i++){
                    remote_port = Integer.parseInt(Remote_ports[i]);
                    Log.d(TAG,"sending list to avd" + remote_port);
                    sendMessage(l,remote_port);

                }
            }


            Log.d(TAG,"send client");
        } catch (UnknownHostException e) {
            Log.e(SimpleDhtProvider.TAG, "ClientTask UnknownHostException", e);
        } catch (IOException e) {
            Log.e(SimpleDhtProvider.TAG, "ClientTask socket IOException", e);
        }
        return null;
    }


    private void sendMessage(Object msgTosend, Integer remotePort) throws IOException {

        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), remotePort);
        Log.d(TAG,"client socket");

        OutputStream os = socket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(msgTosend);
        oos.close();
        os.close();
        socket.close();

    }


}
