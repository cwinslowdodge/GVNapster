package com.company;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

/**
 * Created by haksaver on 10/31/16.
 */
public class ClientToServer {

    public ClientToServer() {
        //Our constructor, not sure what it needs honestly.

    }

    Socket connect(String ipAddr, int port, String ourIP, String ourSpeed) {
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
            if (response.equals("Response: 220 Welcome to JFTP.")) { //TODO response message needs finalizing.
                inputS.close();
                sendServerMetaData(controlConnection, ourIP, ourSpeed);
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


    public Object[][] searchServer(String searchingFor, Socket serverSocket) {
        //TODO should be ready but making sure the central server and the peers still needs to happen.
        try {
            BufferedReader inputS = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            BufferedWriter outputS = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));
            //sending stuff to the server
            outputS.write("SEARCH " + searchingFor + " \r\n");
            outputS.flush();

            String resultsSize = "";
            while(resultsSize.equals("")){
                resultsSize = inputS.readLine(); //read the size of the results array,
            }

            Object[][] ourData = new Object[Integer.parseInt(resultsSize)][3];

            String[] instanceData;
            for(int x = 0; x < Integer.parseInt(resultsSize); x++){
                instanceData = inputS.readLine().split(","); //might not always be divided with commas
                ourData[x][0] = instanceData[0];
                ourData[x][1] = instanceData[1];
                ourData[x][2] = instanceData[2];
            }

            return ourData;
        } catch (Exception e) {
            System.out.println("Something happened with search"); //TODO need to handle null results in the view
            return null;
        }
    }

    public void sendServerMetaData(Socket serverSocket, String ipAddress, String speed){
        //TODO THEORETICALLY DONE BUT NEEDS TESTING ONCE THE VIEW AND SERVER ARE IN PLACE/READY also needs better exception
        //TODO handling.
        try {
            DocumentBuilderFactory myDocFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder myDocBuild = myDocFact.newDocumentBuilder();

            File origMetaFile = new File("./data/meta.xml");
            Document ourOrigFile = myDocBuild.parse(origMetaFile);

            Node connectionNode = ourOrigFile.createElement("Connection");

            Element ipAddr = ourOrigFile.createElement("ip address");
            ipAddr.appendChild(ourOrigFile.createTextNode(ipAddress));

            Element linkSpeed = ourOrigFile.createElement("link Speed");
            linkSpeed.appendChild(ourOrigFile.createTextNode(speed));

            connectionNode.appendChild(ipAddr);
            connectionNode.appendChild(linkSpeed);

            ourOrigFile.getFirstChild().appendChild(connectionNode); //need first child so we are not trying to append to the
            //unchangeable root of the xml file.

            File metaToSend = new File("./data/toServer.xml");

            //this stuff down here is the stuff that saves the file back to our disk, thus allowing us to resend it.
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(ourOrigFile);
            StreamResult result = new StreamResult(metaToSend);
            transformer.transform(source, result);

            //file should now be created so send it out via the data output stream.
            FileInputStream ourFileToRead = new FileInputStream(metaToSend);
            BufferedInputStream dataFromFile = new BufferedInputStream(ourFileToRead);

            byte[] ourBytes = new byte[ (int) metaToSend.length()];

            int hi = dataFromFile.read(ourBytes, 0, ourBytes.length);

            BufferedOutputStream dataOut = new BufferedOutputStream(serverSocket.getOutputStream());

            dataOut.write(ourBytes, 0, ourBytes.length); //shouldn't need a flush thanks to the close, but doing it anyways.
            dataOut.flush();
            dataOut.close();

            //still need to delete the file afterwards.
            Files.deleteIfExists(metaToSend.toPath());

        } catch (IOException e) {
            System.out.println("Whoops in metadata opening");
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            System.out.println("Whoops in parser config");
            e.printStackTrace();
        } catch (SAXException e) {
            System.out.println("Whoops in SAX stuff");
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            System.out.println("Whoops in transformer configuration.");
            e.printStackTrace();
        } catch (TransformerException e) {
            System.out.println("Whoops in transformer transforming");
            e.printStackTrace();
        }
    }
}
