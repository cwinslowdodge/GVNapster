package com.company;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class DataRequest implements Runnable{
    final static String CRLF = "\r\n";
    Socket socket;
    String command;
    private volatile boolean error;
    private volatile boolean complete = false;

    public DataRequest(Socket socket, String command) throws Exception {
        this.socket = socket;
        this.command = command;
    }

    public void run(){
        try {
            processRequest();
        }
        catch (Exception e)
        {
            error = true;
        }
    }

    private void processRequest() throws Exception {
        // output stream
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        DataInputStream  is = new DataInputStream(socket.getInputStream());

            if (command.contains("RETR")) {
                String fileName = command.substring(5); //gets just the file name for our method to use.
                try {
                    ArrayList<String> ourFile = getFile(fileName);

                    for (String lineContents : ourFile) {
                        os.writeUTF(lineContents);
                        os.flush();
                    }
                    os.writeUTF("EOF" + CRLF);
                    os.flush();
                    error = false;
                } catch (Exception e) {
                    //response for a file not found error that would then be written out and the downloading of the file wouldn't happen on the
                    //client end.
                    throw e;
                }
            }

            is.close();
            os.close();
            socket.close();
    }

    private ArrayList<String> getFile(String fileName) throws Exception {
        ArrayList<String> export = new ArrayList<String>();
        File targetFile = new File("./data/" + fileName);
        BufferedReader fileReader = new BufferedReader(new FileReader(targetFile));
        String curLine;
        while ((curLine = fileReader.readLine()) != null) {
            export.add(curLine);
        }
        fileReader.close();
        return export;

        }

    public boolean getError(){
        return error;
    }
}
