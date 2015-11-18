package edu.buffalo.cse.cse486586.simpledht;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;

/**
 * Created by priya on 3/30/15.
 */
public class ServerTask extends AsyncTask<ServerSocket,String,Void> {

    static final String TAG = SimpleDhtActivity.class.getSimpleName();
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";
    ArrayList<Node_Info> as = new ArrayList<>();



    @Override
    protected Void doInBackground(ServerSocket... serverSockets) {

        ServerSocket serversocket = serverSockets[0];
        Socket socket = null;
        String[] received_msg;


        while (true) {
            try {
                Log.d(TAG, "waiting for client");
                socket = serversocket.accept();
                InputStream is = socket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                Object incomingMsg = ois.readObject();
                Log.d(TAG, "server socket");

                if (incomingMsg instanceof String) {
                    String inputLine = incomingMsg.toString();

                    received_msg = inputLine.split(";");
                    Log.d(TAG, "received message");

                    Log.d(TAG, "first" + received_msg[0]);
                    Log.d(TAG, "second" + received_msg[1]);
                    Log.d(TAG, "third" + received_msg[2]);

                    if (received_msg[0].contains("join")) {
                        Log.d(TAG, "check join equals");
                        if (as.isEmpty()) {
                            try {
                                Node_Info n1 = new Node_Info();
                                n1.set_mynode("5554");
                                n1.setHash("5554");
                                as.add(n1);
                            } catch (NoSuchAlgorithmException e) {
                                Log.e(TAG, "cannot add to list");
                            }

                        }

                        ProcessNodeJoin(received_msg[1]);
                        Log.d(TAG, "node joined");
                        NodeList l = new NodeList();
                        l.set_list(as);
                        Log.d(TAG, "will enter client");


                        new Client().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, l);


                    } else if (received_msg[0].contains("update")) {
                        SimpleDhtProvider.Set_successor(received_msg[2]);
                        SimpleDhtProvider.Set_predecessor(received_msg[3]);
                        logPreSucc();
                    } else if (received_msg[0].equals("insert")) {
                        ContentValues cv = new ContentValues();
                        cv.put(KEY_FIELD, received_msg[2]);
                        cv.put(VALUE_FIELD, received_msg[3]);
                        Log.d("%%%%%%%%", "received insert msg");
                        SimpleDhtProvider.mContentResolver.insert(SimpleDhtProvider.mUri, cv);
                    } else if (received_msg[0].equals("query")) {
                        Log.d("i got request", "....");
                        HashMap<String, String> h = new HashMap<>();
                        Cursor c = SimpleDhtProvider.mContentResolver.query(SimpleDhtProvider.mUri, null, received_msg[2], null, null);
                        c.moveToFirst();

                        while (!c.isAfterLast()) {

                            String returnKey = c.getString(0);
                            String returnValue = c.getString(1);
                            c.moveToNext();
                            h.put(returnKey, returnValue);
                        }


                        Resultquery rq = new Resultquery();
                        rq.set_list(h);
                        rq.set_toid(received_msg[3]);

                        Log.d("map sending", "list size " + h.size());
                        new Client().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, rq);
                    } else if (received_msg[0].equals("query1")) {
                        Log.d("chusko", "from node=" + received_msg[3] + " key= " + received_msg[2]);
                        Cursor c = SimpleDhtProvider.mContentResolver.query(SimpleDhtProvider.mUri, null, received_msg[2], null, null);

                        c.moveToFirst();
                        String returnKey = c.getString(0);
                        String returnValue = c.getString(1);

                        Log.d("chuskunava", "key " + returnKey + " value " + returnValue);

                        String singleQueryResult = "single_query" + ";" + received_msg[3] + ";" + returnValue;
                        Object sqr = singleQueryResult;
                        new Client().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, sqr);


                    } else {
                        Log.d("vachinda", "value received = " + received_msg[2]);
                        SimpleDhtProvider.keyval = received_msg[2];
                        SimpleDhtProvider.lock = false;

                    }
                }


            else if(incomingMsg instanceof NodeList){
                    Log.d(TAG,"received list");
                    NodeList nl = (NodeList)incomingMsg;
                    SimpleDhtProvider.list = nl.get_list();
                }

                else if(incomingMsg instanceof Resultquery){
                    Log.d("I came here",".....");

                    Resultquery r = (Resultquery) incomingMsg;
                    Log.d("size","of" + r.get_list().size());

                    SimpleDhtProvider.finalList.add(r.get_list());
                    Log.d("size","of" + SimpleDhtProvider.finalList.size());
                    if(SimpleDhtProvider.finalList.size()==SimpleDhtProvider.list.size()){
                        SimpleDhtProvider.lock = false;
                    }


                }




             } catch (IOException e) {
                Log.e(SimpleDhtProvider.TAG, "Server Exception", e);
            } catch (NoSuchAlgorithmException e) {
                Log.e(SimpleDhtProvider.TAG, "algo error");
            } catch (ClassNotFoundException e) {
                Log.e(TAG,"error at server",e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e(SimpleDhtProvider.TAG, "Unable to close socket", e);
                }
            }
        }
    }


    private void ProcessNodeJoin(String joinRequestNode) throws NoSuchAlgorithmException {


        String FromId = joinRequestNode;
        String update_message = null;
        Object up = null;

        Node_Info n2 = new Node_Info();
        n2.set_mynode(FromId);
        n2.setHash(FromId);
        as.add(n2);

        Collections.sort(as);
        SimpleDhtProvider.list = as;

        if (as.size() == 2) {
            as.get(0).setpre(as.get(1).get_mynode());
            as.get(0).setsucc(as.get(1).get_mynode());
            as.get(1).setpre(as.get(0).get_mynode());
            as.get(1).setsucc(as.get(0).get_mynode());

            if (as.get(0).get_mynode().equals(FromId)) {
                update_message = "update" + ";" + FromId + ";" + as.get(0).getsuc() + ";" + as.get(0).getpre();
                up = update_message;
            } else {
                update_message = "update" + ";" + FromId + ";" + as.get(1).getsuc() + ";" + as.get(1).getpre();
                up = update_message;
            }

            new Client().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, up);


        } else {
            for (int i = 0; i < as.size(); i++) {
                if (i == 0) {
                    update_message = "update" + ";" + as.get(i).get_mynode() + ";" + as.get(1).get_mynode() + ";" + as.get(as.size() - 1).get_mynode();
                    up = update_message;
                    new Client().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, up);
                } else if (i == as.size() - 1) {
                    update_message = "update" + ";" + as.get(i).get_mynode() + ";" + as.get(0).get_mynode() + ";" + as.get(i - 1).get_mynode() ;
                    up = update_message;
                    new Client().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, up);

                } else {
                    update_message = "update" + ";" + as.get(i).get_mynode() + ";" + as.get(i + 1).get_mynode() + ";" + as.get(i - 1).get_mynode() ;
                    up = update_message;
                    new Client().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, up);

                }
            }
        }
    }


/*private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }*/



    private void logPreSucc() {

     Log.d(SimpleDhtProvider.TAG, "UPDATE::" + SimpleDhtProvider.predecessor + "-->"
                + SimpleDhtProvider.my_id + "-->"
                + SimpleDhtProvider.successor);
    }



}
