package com.company;

import java.io.*;
import java.net.Socket;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 * Created by Blaze on 10/23/16.
 */
public class ClientToPeer{

    public ClientToPeer() {
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
            System.out.println("Things happened during the quit.");
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
                    File localFile = new File("./data/" + ourFile);
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

    public void getFileMetaData(String ourFile, Socket givenSocket, int givenPort) {
        //TODO theoretically should be working, xml stuff tested by itself in a separate project but never know
        //TODO when it comes to networking stuff what weirdness will happen.
        try {
            Boolean newFile = true;
            Element toChange = null;

            DocumentBuilderFactory myDBF = DocumentBuilderFactory.newInstance();
            DocumentBuilder myDB = myDBF.newDocumentBuilder();

            File localFile = new File("./data/meta.xml"); //parse the xml file into a document.

            Document localXML = myDB.parse(localFile);

            Element myRootElement = localXML.getDocumentElement();

            NodeList ourNodes = myRootElement.getChildNodes();

            for(int x = 0; x< ourNodes.getLength(); x++){
                if (ourNodes.item(x).getNodeType() == Node.ELEMENT_NODE) {

                    Element eHere = (Element) ourNodes.item(x);

                    if (eHere.getNodeName().contains("file")) {
                        String fileName = eHere.getElementsByTagName("name").item(0).getTextContent();
                        if(fileName.equals(ourFile)){
                            newFile = false; //set newFile to false so that we know to edit our element instead of make a new one.
                            toChange = eHere; //saving the element that needs changing so we don't have to search for it twice.
                        }
                    }
                }
            }

            BufferedReader inputS = new BufferedReader(new InputStreamReader(givenSocket.getInputStream()));
            BufferedWriter outputS = new BufferedWriter(new OutputStreamWriter(givenSocket.getOutputStream()));

            outputS.write("RETR_M " + ourFile); //RETR_M for meta data
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

                    //from here on down is when the xml data receiving and rewriting would happen.
                    //also where we would need to check for existing files in our meta data file that just need
                    //a description updating.

                    String readLine = "ping";

                    while (true) {

                        do {
                            readLine = serverInput.readUTF();
                        }while(readLine.equals(""));//this should handle blank lines being read/sent

                        if (readLine.equals("EOF\r\n")) {
                            break; //done updating the file here
                        } else {
                           //line received here to make the new node or update the existing node description etc.
                            if(newFile){ //add new node to the meta data file etc.
                                Node newFileNode = localXML.createElement("file");

                                Element newName = localXML.createElement("name");
                                newName.appendChild(localXML.createTextNode(ourFile));

                                Element newDesc = localXML.createElement("desc");
                                newDesc.appendChild(localXML.createTextNode(readLine));

                                newFileNode.appendChild(newName);
                                newFileNode.appendChild(newDesc);
                                localXML.getFirstChild().appendChild(newFileNode);
                            }
                            else { //we are updating an existing files meta data.
                                toChange.getElementsByTagName("desc").item(0).setTextContent(readLine);
                            }
                        }
                    }

                    resultString = "";
                    while (resultString.equals("")) {
                        resultString = inputS.readLine();
                    }

                    if (resultString.equals("Response: 226 Closing data connection.")) {
                        inputS.close();
                        outputS.close();

                        //make the new file down here once time is not so sensitive. with the xml factory stuff
                        TransformerFactory transFact = TransformerFactory.newInstance();
                        Transformer transformerObj = transFact.newTransformer();
                        DOMSource mySource = new DOMSource(localXML); //the parsed xml document
                        StreamResult strRes = new StreamResult(localFile); //the local xml file we are rewriting.
                        transformerObj.transform(mySource, strRes);
                    } else {
                        //print out of the error message from the server.
                        System.out.println("Retrieve function did not end properly. Your xml file may not be complete.");
                        inputS.close();
                        outputS.close();
                    }
                } else {
                    System.out.println(resultString + " File not found on the server.");
                }
            }

        } catch (NullPointerException E) {
            System.out.println("Something went wrong with toChange.");
        } catch (ParserConfigurationException e) {
            System.out.println("Something went wrong with parser.");
            e.printStackTrace();
        } catch (SAXException e) {
            System.out.println("Something went wrong with SAX.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Something went wrong with the I/O.");
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            System.out.println("Something went wrong with transformer.");
            e.printStackTrace();
        } catch (TransformerException e) {
            System.out.println("Something went wrong with the transformation itself.");
            e.printStackTrace();
        }
    }
}

