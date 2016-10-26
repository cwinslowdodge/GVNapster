package com.company;

public class Main {

    public static void main(String[] args) {
        ftp_server localServer = new ftp_server();
        Thread forServer = new Thread(localServer);
        forServer.start();

        //then we can move on to creating the client class object to use in the view,
        //then we call the view constructor, pass it our client object, and we can use it in our view
        //to carry our our client based methods, like asking the server who has a file, and then getting the
        //file from that user once their ip address and port numbers are given.
        Client ourClient = new Client();
        //ourClient.connect("12", 12); //unnecessary line to make sure that our client methods actually are reachable through
        //the client object.

        NapsterView ourView = new NapsterView(ourClient);
    }

}
