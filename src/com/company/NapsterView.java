package com.company;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;

/**
 * Created by Blaze on 10/24/16.
 */
public class NapsterView implements ActionListener{

    // start of the overall variables and the connect panel variables.

    Socket centralServer; //our globally accessible socket to the centralized server.

    Client ourClient;

    JFrame ourFrame;

    JPanel overPanel;

    JPanel connectPanel;

    JPanel searchPanel;

    JPanel functionPanel;

    JLabel serverHostname;

    JLabel serverPort;

    JLabel username;

    JLabel speedLabel;

    JTextField serverIP;

    JTextField portNum;

    JTextField usernameBox;

    JButton connectButton;

    JButton quitButton;

    JComboBox linkSelector;

    // start of the search panel variables

    //JLabel searchLabel;

    JLabel keywordLabel;

    JTextField searchField;

    JButton searchButton;

    JTable serverResults;

    // start of the function panel variables.

    //JLabel ftpLabel;

    JLabel commandLabel;

    JTextField commandField;

    JButton goButton;

    JTextArea programOutput;

    String[] jTableHeaders = { "Speed", "Hostname", "File Name"};

    Object[][] ourData = { }; //our data is initially null.

    public NapsterView(Client passedClient){

        ourClient = passedClient;

        ourFrame = new JFrame();

        overPanel = new JPanel(new BorderLayout());

        connectPanel = new JPanel(new GridLayout(2,5));
        connectPanel.setBorder(new TitledBorder(new LineBorder(Color.gray, 3), "Connection"));
        searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(new TitledBorder(new LineBorder(Color.gray, 3), "Search"));
        functionPanel = new JPanel(new BorderLayout());
        functionPanel.setBorder(new TitledBorder(new LineBorder(Color.gray, 3), "FTP"));

        //connect panel items
        serverHostname = new JLabel("Server Hostname: ");
        serverPort = new JLabel("Port: ");
        username = new JLabel("Username: ");
        speedLabel = new JLabel("Speed/kind of link: ");

        serverIP = new JTextField(); //JTextFields for single lines of code, JTextArea's for multi lines of text.
        portNum = new JTextField();
        usernameBox = new JTextField();

        connectButton = new JButton("Connect");
        quitButton = new JButton("Disconnect");

        String[] ourOptions = {"Ethernet", "Wi-Fi", "T-1", "T-3"};
        linkSelector = new JComboBox<String>(ourOptions); //random warning about an unchecked call

        connectButton.addActionListener(this);
        quitButton.addActionListener(this);
        linkSelector.addActionListener(this);

        connectPanel.add(serverHostname);
        connectPanel.add(serverIP);

        connectPanel.add(serverPort);
        connectPanel.add(portNum);

        connectPanel.add(connectButton);

        connectPanel.add(username);
        connectPanel.add(usernameBox);

        connectPanel.add(speedLabel);
        connectPanel.add(linkSelector);

        connectPanel.add(quitButton);

        //search panel items

        keywordLabel = new JLabel("Keyword: ");

        searchField = new JTextField();

        searchButton = new JButton("Search");

        serverResults = new JTable(ourData, jTableHeaders); //our data is empty, need to redefine it and update the table once
        //we hit search and get back some results. EDIT So we update data with the values from the server, then we call
        //serverResults.fireTableDataChanged();

        searchButton.addActionListener(this);

        searchPanel.add(keywordLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        searchPanel.add(serverResults, BorderLayout.SOUTH);

        //function panel items

        commandLabel = new JLabel("Enter Command: ");

        commandField = new JTextField();

        goButton = new JButton("Go");

        programOutput = new JTextArea();

        goButton.addActionListener(this);

        functionPanel.add(commandLabel, BorderLayout.WEST);
        functionPanel.add(commandField, BorderLayout.CENTER);
        functionPanel.add(goButton, BorderLayout.EAST);
        functionPanel.add(programOutput, BorderLayout.SOUTH);

        //putting everything together.
        overPanel.add(connectPanel, BorderLayout.NORTH);
        overPanel.add(searchPanel, BorderLayout.CENTER);
        overPanel.add(functionPanel, BorderLayout.SOUTH);

        ourFrame.add(overPanel);
        ourFrame.pack();
        ourFrame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        //actions performed need to check for the connect and disconnect buttons being pushed to set up
        //and tear down the connection/socket to the central/Database server
        //part of the checking options for connect will be to make sure all of the fields from the connect panel are
        //filled in, all users need to have a username, all connections need a valid port number, etc.

        //with the go button we need to check if its a connect, RETR, or quit command and each of these three
        //depend on a locally created socket that is directed at the peers server after getting the IP
        //of said peer from the searchResults JTable.
        //connect and quit set up the local socket and then retr can be used with it to get lots of stuff from the
        //peer's server.

        //the go button is used to send the keyword for the search term to the central server with the globally created/available
        //socket. there is a search server method that is in the client class that just needs the socket of the server to
        //direct the query at and the string that is being searched for, aka the keyword from the searchField JTextField.

        //all of these will need proper boolean flags to make sure you are not trying to quit before you connect, connect
        //before quitting the last server etc. no retrieving before connecting to a peer server, no quitting a peer server before
        //connecting etc.
    }

}
