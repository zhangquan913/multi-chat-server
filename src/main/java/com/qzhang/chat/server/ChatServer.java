package com.qzhang.chat.server;

import java.io.*;
import java.net.*;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

/**
 * A Chat Server dealing with public and private messages from multiple clients.
 * 
 * @author Quan
 *
 */
public class ChatServer {
    // This chat server can accept up to MAX_CLIENT_COUNT clients' connections.
    private static final int MAX_CLIENT_COUNT = 10;
    private static final ClientThread[] THREADS = new ClientThread[MAX_CLIENT_COUNT];
    private static final String JAR_NAME = "chat-server-<version>.jar";
    private static final int DEFAULT_PORT = 8989;
    private static final String PORT = "port";
    private static Logger logger = Logger.getLogger(ChatServer.class);

    // The server socket.
    private static ServerSocket serverSocket = null;
    // The client socket.
    private static Socket clientSocket = null;

    public static void main(String args[]) {
        Options options = new Options();

        Option port = new Option("p", "port", true, "the server port");
        port.setRequired(false);
        options.addOption(port);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar " + JAR_NAME, options);
            System.exit(1);
            return;
        }

        int portNumber = DEFAULT_PORT;
        if (!cmd.hasOption(PORT)) {
            System.out.println("Now using default port: " + DEFAULT_PORT + "\nTo start server with another port: java -jar "
                    + JAR_NAME + " -p <port>");
        } else {
            portNumber = Integer.parseInt(cmd.getOptionValue(PORT));
        }

        /*
         * Open a server socket on the portNumber (default 8989).
         */
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        logger.info("Server socket opened.");

        /*
         * For each connection create a client socket and pass it to a new client
         * thread.
         */
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                int i = 0;
                for (i = 0; i < MAX_CLIENT_COUNT; i++) {
                    if (THREADS[i] == null) {
                        (THREADS[i] = new ClientThread(clientSocket, THREADS)).start();
                        break;
                    }
                }
                if (i == MAX_CLIENT_COUNT) {
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("Chat Server too busy. Please try again later.");
                    os.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }
}