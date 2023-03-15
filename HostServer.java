/*
1. Name: Tirth Sharaf

2. Date: 2023-03-02

3. Java version: 19.0.1.1

4. Precise command-line compilation examples / instructions:

> javac HostServer.Java

5. Precise examples / instructions to run this program:

In separate shell windows:

> java HostServer

>start a web browser of your choice and point it to http://localhost:4242. Enter some text and press
the submit button to simulate a state-maintained conversation.


6. Full list of files needed for running the program:

 a. HostServer.java

 7. Thanks

 Thanks John Reagan for updates to original code by Clark Elliott.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * This class represents a server-side agent that listens for incoming requests
 * from clients and responds to them based on its current state.
 */
class AgentWorker extends Thread {

    // Declare instance variables
    Socket sock;
    agentHolder parentAgentHolder;
    int localPort;

    /**
     * Creates a new AgentWorker object with the specified socket, local port,
     * and parent agent holder.
     *
     * @param s the socket representing the client connection
     * @param prt the local port number
     * @param ah the parent agent holder
     */
    AgentWorker (Socket s, int prt, agentHolder ah) {
        sock = s;
        localPort = prt;
        parentAgentHolder = ah;
    }

    /**
     * Runs the agent worker thread, handling incoming requests from clients and
     * updating the agent state as needed.
     */
    public void run() {

        // Declare local variables
        PrintStream out = null;
        BufferedReader in = null;

        String NewHost = "localhost";

        int NewHostMainPort = 4242;
        String buf = "";
        int newPort;
        Socket clientSock;
        BufferedReader fromHostServer;
        PrintStream toHostServer;

        try {  // Initialize output and input streams using the socket
            out = new PrintStream(sock.getOutputStream());
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            // read the request from the client
            String inLine = in.readLine();

            // Initialize a StringBuilder to build the HTML response
            StringBuilder htmlString = new StringBuilder();

            // print the request to the console
            System.out.println();
            System.out.println("Request line: " + inLine);

            // check if the request is to migrate to a new host
            if(inLine.indexOf("migrate") > -1) {

                // connect to the new host and request a new port number
                clientSock = new Socket(NewHost, NewHostMainPort);
                fromHostServer = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));

                toHostServer = new PrintStream(clientSock.getOutputStream());

                // Send a request to the new host server to host this agent and send the port
                toHostServer.println("Please host me. Send my port! [State=" + parentAgentHolder.agentState + "]");
                toHostServer.flush();

                // read the new port number from the response
                for(;;) {

                    buf = fromHostServer.readLine();
                    if(buf.indexOf("[Port=") > -1) {
                        break;
                    }
                }

                // Parse the port number from the response
                String tempbuf = buf.substring( buf.indexOf("[Port=")+6, buf.indexOf("]", buf.indexOf("[Port=")) );
                newPort = Integer.parseInt(tempbuf);

                // Print the new port number to the console
                System.out.println("newPort is: " + newPort);


                // send a response to the client indicating that the agent is migrating to a new host
                htmlString.append(AgentListener.sendHTMLheader(newPort, NewHost, inLine));
                htmlString.append("<h3>We are migrating to host " + newPort + "</h3> \n");
                htmlString.append("<h3>View the source of this page to see how the client is informed of the new location.</h3> \n");
                htmlString.append(AgentListener.sendHTMLsubmit());

                // close the parent listening loop
                System.out.println("Killing parent listening loop.");

                ServerSocket ss = parentAgentHolder.sock;

                ss.close();


            } else if(inLine.indexOf("person") > -1) {

                // increment the agent's state and send a response to the client
                parentAgentHolder.agentState++;

                // Build the HTML response to display the current state
                htmlString.append(AgentListener.sendHTMLheader(localPort, NewHost, inLine));
                htmlString.append("<h3>We are having a conversation with state   " + parentAgentHolder.agentState + "</h3>\n");
                htmlString.append(AgentListener.sendHTMLsubmit());

            } else {

                // if the request is not for migration or conversation, return an error message to the client
                htmlString.append(AgentListener.sendHTMLheader(localPort, NewHost, inLine));
                htmlString.append("You have not entered a valid request!\n");
                htmlString.append(AgentListener.sendHTMLsubmit());


            }

            // send the HTML response to the client
            AgentListener.sendHTMLtoStream(htmlString.toString(), out);

            // close the socket
            sock.close();


        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

}

/**
 * This class represents a holder for an agent and its current state.
 */
class agentHolder {
    ServerSocket sock; // The ServerSocket used to listen for client requests
    int agentState; // The current state of the agent

    /**
     * Creates a new agent holder object with the specified server socket.
     *
     * @param s the server socket
     */
    agentHolder(ServerSocket s) { sock = s;}
}

/**
 * This class represents a listener for incoming client connections on a specific
 * port, creating a new agent worker thread for each connection.
 */
class AgentListener extends Thread {
    Socket sock; // A Socket object to communicate with the client
    int localPort; // The local port number

    /**
     * Creates a new AgentListener object with the specified socket and local port.
     *
     * @param As the socket representing the client connection
     * @param prt the local port number
     */
    AgentListener(Socket As, int prt) {  // Constructor to set the Socket object and port number
        sock = As;
        localPort = prt;
    }

    int agentState = 0; // The initial state of the agent is 0

    /**
     * Runs the agent listener thread, handling incoming client connections and
     * creating a new agent worker thread for each one.
     */
    public void run() {

        BufferedReader in = null; // A BufferedReader object to read data from the client
        PrintStream out = null; // A PrintStream object to send data to the client
        String NewHost = "localhost"; // The hostname of the local machine
        System.out.println("In AgentListener Thread"); // Print a message to indicate that the thread has started

        try {
            String buf; // A string to hold the data received from the client
            out = new PrintStream(sock.getOutputStream());  // Get the output stream of the socket to send data to the client
            in =  new BufferedReader(new InputStreamReader(sock.getInputStream()));  // Get the input stream of the socket to read data from the client


            buf = in.readLine(); // Read the first line of input from the client

            // If the input contains a state parameter, extract the value and set it as the current state of the agent
            if(buf != null && buf.indexOf("[State=") > -1) {

                String tempbuf = buf.substring(buf.indexOf("[State=")+7, buf.indexOf("]", buf.indexOf("[State=")));
                agentState = Integer.parseInt(tempbuf);
                System.out.println("agentState is: " + agentState);

            }

            System.out.println(buf); // Print the input received from the client

            StringBuilder htmlResponse = new StringBuilder(); // A StringBuilder object to hold the HTML response to be sent to the client

            // Create the HTML response with the current port number, hostname, and input received from the client
            htmlResponse.append(sendHTMLheader(localPort, NewHost, buf));
            htmlResponse.append("Now in Agent Looper starting Agent Listening Loop\n<br />\n");
            htmlResponse.append("[Port="+localPort+"]<br/>\n");
            htmlResponse.append(sendHTMLsubmit());

            sendHTMLtoStream(htmlResponse.toString(), out); // Send the HTML response to the client

            // Create a new server socket on the specified port
            ServerSocket servsock = new ServerSocket(localPort,2);

            // Create a new agent holder object to hold the server socket and agent state
            agentHolder agenthold = new agentHolder(servsock);
            agenthold.agentState = agentState;


            while(true) {   // Loop indefinitely, accepting connections to the server socket

                sock = servsock.accept();

                System.out.println("Got a connection to agent at port " + localPort);

                // Create a new agent worker thread to handle the connection
                new AgentWorker(sock, localPort, agenthold).start();
            }

        } catch(IOException ioe) {

            // If an exception occurs, print an error message
            System.out.println("Either connection failed, or just killed listener loop for agent at port " + localPort);
            System.out.println(ioe);
        }
    }

    /**
     * Sends an HTML header to the client with the specified local port, host,
     * and input text.
     *
     * @param localPort the local port number
     * @param NewHost the host name
     * @param inLine the input text
     * @return the HTML header as a string
     */
    static String sendHTMLheader(int localPort, String NewHost, String inLine) {

        StringBuilder htmlString = new StringBuilder();

        // Add the HTML tags for the header and body
        htmlString.append("<html><head> </head><body>\n");

        // Add a header indicating the submission is for the specified port and host
        htmlString.append("<h2>This is for submission to PORT " + localPort + " on " + NewHost + "</h2>\n");

        // Add a sub-header indicating what the user sent in
        htmlString.append("<h3>You sent: "+ inLine + "</h3>");

        // Add an HTML form to allow the user to input new text
        htmlString.append("\n<form method=\"GET\" action=\"http://" + NewHost +":" + localPort + "\">\n");
        htmlString.append("Enter text or <i>migrate</i>:");
        htmlString.append("\n<input type=\"text\" name=\"person\" size=\"20\" value=\"YourTextInput\" /> <p>\n");

        return htmlString.toString();
    }

    /**
     * Sends an HTML submit button to the client.
     *
     * @return the HTML submit button as a string
     */
    static String sendHTMLsubmit() {
        return "<input type=\"submit\" value=\"Submit\"" + "</p>\n</form></body></html>\n";
    }

    /**
     * Sends the specified HTML string to the client through the specified
     * output stream.
     *
     * @param html the HTML string
     * @param out the output stream to send the HTML through
     */
    static void sendHTMLtoStream(String html, PrintStream out) {

        // Set the HTTP response headers
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Length: " + html.length());
        out.println("Content-Type: text/html");
        out.println("");

        // Send the HTML response to the output stream
        out.println(html);
    }

}

/**
 * This class represents a host server that listens for incoming client connections
 * and creates a new agent listener for each one on a different port.
 */
public class HostServer {

    /**
     * The next port number to be used for starting a new AgentListener.
     */
    public static int NextPort = 3000;

    /**
     * The main method of the HostServer class.
     * @param a an array of command-line arguments for the HostServer
     * @throws IOException if an I/O error occurs while creating the ServerSocket
     */
    public static void main(String[] a) throws IOException {
        int q_len = 6; // maximum number of queued incoming connections
        int port = 4242;// the port number that the server will listen on
        Socket sock;

        // create a new ServerSocket with the specified port number and queue length
        ServerSocket servsock = new ServerSocket(port, q_len);

        // print a message indicating that the server has started
        System.out.println("Elliott/Reagan DIA Master receiver started at port 4242.");
        System.out.println("Connect from 1 to 3 browsers using \"http:\\\\localhost:4242\"\n");

        while(true) {  // loop indefinitely, accepting incoming connections and starting new AgentListeners for each one

            NextPort = NextPort + 1; // increment the NextPort counter

            sock = servsock.accept();  // accept a new incoming connection

            System.out.println("Starting AgentListener at port " + NextPort);  // print a message indicating that a new AgentListener is starting

            new AgentListener(sock, NextPort).start(); // start a new AgentListener to handle the connection on a new thread
        }

    }
}