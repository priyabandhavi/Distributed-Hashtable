package edu.buffalo.cse.cse486586.simpledht;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimpleDhtProvider extends ContentProvider {

    static final String TAG = SimpleDhtActivity.class.getSimpleName();

    public static String predecessor, successor, my_id, myPort;
    int Serverport = 10000;
    public static ContentResolver mContentResolver;
    public static Uri mUri;

    public static boolean lock = true;
    String[] Remote_ports = {"11108","11112","11116","11120","11124"};


    public static void Set_predecessor(String pred) {
        predecessor = pred;
    }

    public static String Get_predecessor() {
        return predecessor;
    }

    public static void Set_successor(String succ) {
        successor = succ;
    }

    public static String Get_Successor() {
        return successor;
    }

    public static String fromid = null;

    public static ArrayList<Node_Info> list = new ArrayList<>();

    public static HashMap<String,String> queryResult;

    public static ArrayList<HashMap> finalList = null;

   public static String keyval = null;

    /*public static void from_id(String from){
        fromid = from;

    }
    public static String get_from(){
        return fromid;
    } */


    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }


    @Override
    public boolean onCreate() {
        TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        my_id = portStr;
        predecessor = my_id;
        successor = my_id;
        mContentResolver = getContext().getContentResolver();
        mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");


        try {
            ServerSocket serverSocket = new ServerSocket(Serverport);
            Log.d(TAG, "connect socket");
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
            Log.d(TAG, "socket message");
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
        }

        if (!my_id.equals("5554")) {
            Log.d(TAG, "check port");
            String join_message = "join" + ";" + my_id + ";" + "5554";
            Object jjm = join_message;
            Log.d(TAG, "make join message");
            new Client().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, jjm);
            Log.d(TAG, "send to client");


        }

        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        String filename = selection;
        StringBuilder sb = new StringBuilder();
        int data;
        String value = null;
        String[] ColumnNames = {"key", "value"};
        MatrixCursor mc = new MatrixCursor(ColumnNames);

        File dir = getContext().getFilesDir();

        if(!filename.equals("\"@\"")&&!filename.equals("\"*\"")&& list.isEmpty()){

            Log.d("Does it come here?","Jaffa Priya");
            try {

                FileInputStream fs = getContext().openFileInput(filename);
                while ((data = fs.read()) != -1) {
                    sb.append((char) data);

                }
                fs.read();
                fs.close();

            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found,Query fail");
            } catch (IOException e) {
                Log.e(TAG, "Query fail");
            }
            value = sb.toString();
            sb.setLength(0);

            String[] ColumnValues = {filename,value};
            mc.addRow(ColumnValues);


            return mc;

        }else if (list.isEmpty()) {
            for (File x : dir.listFiles()) {
                try {

                    FileInputStream fs = getContext().openFileInput(x.getName());
                    while ((data = fs.read()) != -1) {
                        sb.append((char) data);

                    }
                    fs.read();
                    fs.close();

                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found,Query fail");
                } catch (IOException e) {
                    Log.e(TAG, "Query fail");
                }
                value = sb.toString();
                sb.setLength(0);

                String[] ColumnValues = {x.getName(), value};                                 //REFERENCE: Android Documentation
                mc.addRow(ColumnValues);
                Log.d("################# " + x.getName(), value);


            }
            return mc;


        } else if (filename.equals("\"@\"")) {

            Log.d("&&&&&&&&&&&&&&", "priya");

            for (File x : dir.listFiles()) {
                Log.d("&&&&&&&&&&&&&&", "priya");

                 try {

                    FileInputStream fs = getContext().openFileInput(x.getName());
                    while ((data = fs.read()) != -1) {
                        sb.append((char) data);

                    }
                    fs.read();
                    fs.close();

                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found,Query fail");
                } catch (IOException e) {
                    Log.e(TAG, "Query fail");
                }
                value = sb.toString();
                sb.setLength(0);


                String[] ColumnValues = {x.getName(), value};                                 //REFERENCE: Android Documentation
                mc.addRow(ColumnValues);
                Log.d("################# " + x.getName(), value);


            }
            return mc;


        } else if (filename.equals("\"*\"")) {
            finalList = new ArrayList<>();
            Log.d("trying *","..");
            for (int i = 0; i < list.size(); i++) {
                String query_message = "query" + ";" + list.get(i).get_mynode() + ";" + "\"@\"" + ";" + SimpleDhtProvider.my_id;
                Object qm = query_message;
                new Client().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, qm);
            }
            while (lock) {

            }
            lock = true;


            for (int j = 0; j < finalList.size(); j++) {
                HashMap<String, String> hm = finalList.get(j);
                Set set = hm.entrySet();
                Iterator it = set.iterator();
                while (it.hasNext()) {
                    Map.Entry me = (Map.Entry) it.next();

                    String[] ColumnValues = {(String) me.getKey(), (String) me.getValue()};                                 //REFERENCE: Android Documentation
                    mc.addRow(ColumnValues);

                }



            }
            return mc;
        } else {

            try {

                Log.d(TAG, "entering try");
                int next_index = 0;
                Node_Info n = new Node_Info();
                n.set_mynode(filename);
                n.setHash(filename);
                list.add(n);
                Collections.sort(list);


                int key_index = list.indexOf(n);
                Log.d(TAG, "index of n..." + String.valueOf(key_index));
                if (key_index == list.size() - 1) {
                    next_index = 0;
                } else {
                    next_index = key_index + 1;
                }

                String send_node = list.get(next_index).get_mynode();
                Log.d("*************************", send_node);
                list.remove(n);


                if (send_node.equals(SimpleDhtProvider.my_id)) {
                    try {

                        FileInputStream fs = getContext().openFileInput(filename);
                        while ((data = fs.read()) != -1) {
                            sb.append((char) data);

                        }
                        fs.read();
                        fs.close();

                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "File not found,Query fail");
                    } catch (IOException e) {
                        Log.e(TAG, "Query fail");
                    }
                    value = sb.toString();
                    sb.setLength(0);

                    String[] ColumnValues = {filename,value};
                    mc.addRow(ColumnValues);


                }

                    //REFERENCE: PA1

                else{
                    String single_query = "query1" + ";" + send_node + ";" + filename + ";" + SimpleDhtProvider.my_id;
                    Object sq = single_query;

                    Log.d("!!!!!!!!!!!!!!!!!!!!!","node= " + send_node + " filename= " + filename);
                    new Client().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, sq);


                    while(lock){


                    }
                    Log.d("out of loop","value is" + keyval);
                    lock = true;
                    String[] ColumnValues = {filename,keyval};
                    mc.addRow(ColumnValues);


                }




            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return mc;

        }
    }




@Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        Log.d(TAG,"enter insert");




        String key = null;
        String filename = null;
        String value = null;

        for (Map.Entry x : contentValues.valueSet()) {
            if (x.getKey().equals("key")) {
                key = (String) x.getValue();
            } else {
                value = (String) x.getValue();
            }
        }

     Log.d(TAG,"key is" + "...." + key);
        Log.d(TAG, "value is" + "..." + value);

        if(list.isEmpty()){
            filename = key;                                                             //REFERENCE: PA1
            try {
                FileOutputStream output = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                output.write(value.getBytes());
                output.close();
            } catch (Exception e) {
                Log.e(TAG, "Insert fail");
            }

            Log.v("insert", contentValues.toString());
            return uri;
        }



        try {

            Log.d(TAG,"entering try");
            int next_index = 0;
            Node_Info n = new Node_Info();
            n.set_mynode(key);
            n.setHash(key);
            list.add(n);
            Collections.sort(list);


            for(int i = 0; i <list.size(); i++){
                Log.d(TAG, i + "element..." + list.get(i).get_mynode());


            }



            int key_index = list.indexOf(n);
            Log.d(TAG,"index of n..." + String.valueOf(key_index));
            if (key_index == list.size() - 1) {
                next_index = 0;
            } else {
                next_index = key_index + 1;
            }

            String send_node = list.get(next_index).get_mynode();
            Log.d("*************************",send_node);
            list.remove(n);

            if (send_node.equals(SimpleDhtProvider.my_id)) {

                filename = key;                                                             //REFERENCE: PA1
                try {
                    FileOutputStream output = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                    output.write(value.getBytes());
                    output.close();
                } catch (Exception e) {
                    Log.e(TAG, "Insert fail");
                }

                Log.v("insert", contentValues.toString());
                return uri;
            }



             else {
                String insert_message = "insert" + ";" + send_node + ";" + key + ";" + value;
                Object im = insert_message;
                new Client().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, im);


            }


        } catch (NoSuchAlgorithmException e1) {
            Log.e(SimpleDhtProvider.TAG, "NoSuchAlgorithmException");
        }
        return uri;

    }








    @Override
    public int delete(Uri uri, String s, String[] strings) {
        File dir = getContext().getFilesDir();
        for (File x : dir.listFiles()){
            x.delete();
        }
            return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }





    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA­1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }


}