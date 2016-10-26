package com.company;

import java.io.*;
import java.net.Socket;

/**
 * Created by Blaze on 10/23/16.
 */
public class Client {

    public Client(){
        //Our constructor, not sure what it needs honestly.
    }

    Socket connect(String ipAddr, int port) {
        //connect to the server in question here, and then in the view, have the view send the central server the username and
        //link speed stuff later.
        try {
            Socket listenerConnection = new Socket(ipAddr, port);
            BufferedReader inputFromServer = new BufferedReader(new InputStreamReader(listenerConnection.getInputStream()));
            String connectPort = inputFromServer.readLine();
            System.out.println(connectPort);
            listenerConnection.close(); //might need to murphy proof this by sending out a reply to the server that we got through.

            int intConnectPort = Integer.parseInt(connectPort);
            Socket controlConnection = new Socket(ipAddr, intConnectPort);
            BufferedReader inputS = new BufferedReader(new InputStreamReader(controlConnection.getInputStream()));

            //System.out.println("Connect received.");
            String response = inputS.readLine();
            //System.out.println("response received.");
            if (response.equals("Response: 220 Welcome to JFTP.")) {
                inputS.close();
                return controlConnection; //once we are connected in the view action listener is when we send our file metadata collecion.
            } else {
                return null;
            }

        } catch (Exception e) {
            System.out.println("Connection Exception. " + e.toString());
            return null; //null checks on the other end needed.
        }

    }

    public void quitServer(Socket givenSocket) {
        try {
            System.out.println("Thank you for using the program.");
            BufferedReader inputS = new BufferedReader(new InputStreamReader(givenSocket.getInputStream()));
            BufferedWriter outputS = new BufferedWriter(new OutputStreamWriter(givenSocket.getOutputStream()));
            outputS.write("QUIT\r\n"); //once quit is sent to the central server, our records of what we host must be
            //deleted and the server must say that we quit. no passing of username because then quit doesn't work on the peer
            //to peer servers
            outputS.close();
            inputS.close();
            givenSocket.close();
        } catch (Exception e) {
            System.out.println("Things happened");
        }
    }

    public void getFile(String ourFile, Socket givenSocket, int givenPort) {
        try {
            BufferedReader inputS = new BufferedReader(new InputStreamReader(givenSocket.getInputStream()));
            BufferedWriter outputS = new BufferedWriter(new OutputStreamWriter(givenSocket.getOutputStream()));

            outputS.write("RETR " + ourFile);
            outputS.write("\r\n");
            outputS.flush();


            String resultString = "";
            while (resultString.equals("")) {
                resultString = inputS.readLine();
            }

            //System.out.println("first result "+resultString);

            if (resultString.equals("Response: 225 Data Connection Open.")) {
                Socket dataSocket = new Socket(givenSocket.getInetAddress(), givenPort);
                DataInputStream serverInput = new DataInputStream(dataSocket.getInputStream());

                resultString = "";
                while (resultString.equals("")) {
                    resultString = inputS.readLine();
                }

                //System.out.println(resultString);

                if (!resultString.equals("Response: 426 Something broke.")) {
                    File localFile = new File("./" + ourFile); //TODO decide on where files are retrieved from and being written too.
                    BufferedWriter dataOutToFile = new BufferedWriter(new FileWriter(localFile));
                    String readLine = "ping";

                    while (true) {
                        readLine = serverInput.readUTF();
                        if (!readLine.equals("EOF\r\n")) {
                            dataOutToFile.write(readLine);
                            dataOutToFile.write("\r\n");
                            dataOutToFile.flush();
                        } else {
                            break;
                        }
                    }

                    resultString = "";
                    while (resultString.equals("")) {
                        resultString = inputS.readLine();
                    }

                    if (resultString.equals("Response: 226 Closing data connection.")) {
                        inputS.close();
                        outputS.close();
                        dataOutToFile.close();
                    } else {
                        //print out of the error message from the server.
                        System.out.println("Retrieve function did not end properly. Your file may not be complete.");
                        inputS.close();
                        outputS.close();
                        dataOutToFile.close();
                    }
                } else {
                    System.out.println(resultString + " File not found on the server.");
                }
            }

        } catch (Exception E) {
            System.out.println("Something went wrong with retrieve.");
        }
    }

    public String searchServer(String searchingFor, Socket serverSocket) {
        try {
            BufferedReader inputS = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            BufferedWriter outputS = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));
            //sending stuff to the server
            outputS.write("SEARCH " + searchingFor + " \r\n");
            outputS.flush();

            String results = inputS.readLine(); //way over simplified. needs to be expanded later
            return results;
        } catch (Exception e) {
            System.out.println("Something happened with search");
            return "Whoops";
        }
    }
}

