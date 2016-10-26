package com.company;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class ftp_server implements Runnable {

    public ftp_server(){
        //does nothing, just need it for instantiation.
    }

    public static void main(String[] args) throws Exception {

    }

    private int getOpenPort(){
        Random nextRandom = new Random();
        try{
            int ourPort = nextRandom.nextInt(16299)+49200;
            ServerSocket tester = new ServerSocket(ourPort); //49200 - 65500
            System.out.println("generated our port " + ourPort);
            tester.close();
            return ourPort;
        }
        catch(Exception e){
            return 0;
        }
    }

    @Override
    public void run() {
        try {
            // Create Port
            int port = 3715;

            // if data directory does not exist, create it
            File dir = new File("./data");
            if (dir.isDirectory()) {
                // do nothing
            } else {
                dir.mkdir();
            }

            ServerSocket listenSocket = new ServerSocket(port);

            while (true) {
                // Listen Socket
                Socket connection = listenSocket.accept();

                // Code Loop
                BufferedWriter dictate = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

                int ourPort = 0;
                while (ourPort == 0) {
                    ourPort = getOpenPort();
                }
                System.out.println("ourPort has been made");
                dictate.write(ourPort + "\r\n"); //dont forget your end lines
                dictate.flush(); //dont forget to flush either.
                connection.close();
                ServerSocket serverSocketForUser = new ServerSocket(ourPort);
                System.out.println("server socket made");
                // Listen for TCP

                Socket userSocket = serverSocketForUser.accept();
                System.out.println("Accepted");

                FtpRequest request = new FtpRequest(userSocket);

                // Command thread
                Thread cThread = new Thread(request);

                // Start command thread
                cThread.start(); //watch out for runs. we want starts.
                System.out.println("Ready for more.");
            }

        } catch (Exception e) {
            System.out.println("Something went wrong with host server run.");
        }
    }
}
