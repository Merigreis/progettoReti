package org.example;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class IRCChatServer {
    private static final int PORT = 8887;
    private static final Map<String, List<PrintWriter>> channels = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server avviato sulla porta " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Nuova connessione: " + clientSocket);
            new ClientHandler(clientSocket).start();
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket;
        private PrintWriter out;
        private String channel;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                out.println("MERDA");
                String temp = in.readLine().trim();
                // Chiedi al client di inserire il nome utente
                out.println("Inserire nome utente: ");
                String username = in.readLine().trim();

                out.println("Inserisci ruolo: ");
                String ruolo = in.readLine().trim();

                String userChannel = null;
                if (ruolo.toLowerCase() == "utente") {
                    //Genera automaticamente il nome del canale con l'UUID
                    userChannel = "channel_" + username + "_" + UUID.randomUUID().toString();

                    // Controlla se ci sono già due persone nel canale
                    synchronized (channels) {
                        if (!channels.containsKey(userChannel)) {
                            channels.put(userChannel, new ArrayList<>());
                        }

                    }
                    System.out.println("Ciao " + username + ", benvenuto nel tuo canale: " + userChannel);
                } else if (ruolo.toLowerCase() == "admin") {





















                }


                String message;
                while ((message = in.readLine()) != null) {
                    broadcastMessage(username, message, userChannel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Rimuovi il PrintWriter associato al client dalla lista dei partecipanti al canale
                synchronized (channels) {
                    if (channel != null && channels.containsKey(channel)) {
                        channels.get(channel).remove(out);
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        // Metodo per inviare un messaggio a tutti i client nello stesso canale
        private void broadcastMessage(String username, String message, String userChannel) {
            synchronized (channels) {
                List<PrintWriter> channelUsers = channels.get(userChannel);
                if (channelUsers != null) {
                    for (PrintWriter writer : channelUsers) {
                        writer.println(username + "@" + userChannel + ": " + message);
                    }
                }
            }
        }
    }
}
