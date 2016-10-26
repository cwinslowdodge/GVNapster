package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

final class FtpRequest implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;
    int dataPort = 3716;

    BufferedWriter os;

    // constructor
    public FtpRequest(Socket socket) throws Exception {
        this.socket = socket;
    }

    // runnable run
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e); //use the control connection here for writing out error messages to the client.
        }
    }

    private void processRequest() throws Exception {
        String commandExport = "";

        // output stream
        os = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        // input stream
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Send Welcome Response to Client
        response(os, "Response: 220 Welcome to JFTP." + CRLF);

        // command loop
        //TODO reference: https://en.wikipedia.org/wiki/List_of_FTP_server_return_codes
        String commandRequest;
        while(true) {
            String commandLine = br.readLine();
            System.out.println(commandLine);

            String[] clientCommand = commandLine.split(" ");//tokens.nextToken();


            if(clientCommand.length == 1)
            {
                commandExport = clientCommand[0];
            }
            else{
                String clientArg = clientCommand[1];//tokens.nextToken();
                commandExport = clientCommand[0] + " " + clientArg;
            }


            if(clientCommand[0].equals("RETR")) {
                //response(os, "Response: 202 RETR not implemented." + CRLF);
                boolean report = false;
                ServerSocket dataSock = new ServerSocket(dataPort);

                response(os, "Response: 225 Data Connection Open." + CRLF);
                while(true) {
                    Socket dataConn = dataSock.accept();

                    // Create Data Handler
                    DataRequest dataHandler = new DataRequest(dataConn, commandExport);

                    // Data handler Thread
                    Thread dThread = new Thread(dataHandler);

                    // run
                    dThread.start();
                    dThread.join();
                    report = dataHandler.getError();
                    if(!report){
                        response(os, "Response: 226 Closing data connection." + CRLF);
                    }
                    else{
                        response(os, "Response: 426 Something broke." + CRLF);
                    }

                    dataSock.close(); //added to close things
                    break; //added to get us back to listening for other instructions.
                }
                response(os, "Response: 226 Closing data connection." + CRLF);

            }

            if(clientCommand[0].equals("QUIT")) {
                response(os, "Response: 221 Closing connection." + CRLF);
                break;
            }

        }

        os.close();
        br.close();
        socket.close();
    }

    private void response(BufferedWriter os, String res) throws Exception {
        System.out.println(res);
        os.write(res, 0, res.length());
        os.write("\r\n", 0, "\r\n".length());
        os.flush();
    }

    private ArrayList<String> getDirectory() {
        ArrayList<String> listContents = new ArrayList<String>();

        File dir = new File("./data"); //one dot for in IDE, two for CLI usage
        File[] dirList = dir.listFiles();

        for (File file : dirList) {
            listContents.add(file.getName());
        }

        return listContents;
    }

}
