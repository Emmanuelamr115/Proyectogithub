//Chat Servidor:

package com.mycompany.ChatServidor;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

/**
 *
 * @Author Equipo1
 */

/**
 * La clase ChatServer maneja un servidor de chat que acepta conexiones de múltiples clientes,
 * recibe mensajes de los clientes y los retransmite a todos los clientes conectados.
 */

public class ChatServidor {
    private static final int PORT = 12345;
    private static final String HOST = "0.0.0.0";  // Cambia esto a la IP deseada
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static Map<String, PrintWriter> userWriters = new HashMap<>();
    private static Map<String, String> userImages = new HashMap<>();

    /**
     * El método principal que inicia el servidor de chat.
     *
     * @param args Los argumentos de la línea de comandos (no se utilizan).
     */
    public static void main(String[] args) {
        // Crear y mostrar la ventana del servidor
        SwingUtilities.invokeLater(() -> {
            ServerWindow serverWindow = new ServerWindow();
            serverWindow.setVisible(true);
        });

        System.out.println("Chat server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(HOST))) {
            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * La clase ServerWindow representa la interfaz gráfica del servidor de chat.
     */
    private static class ServerWindow extends JFrame {
        private JLabel statusLabel;

        /**
         * Constructor que inicializa la ventana del servidor.
         */
        public ServerWindow() {
            setTitle("Chat Server");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(300, 100);
            setLocationRelativeTo(null); // Centrar la ventana en la pantalla

            statusLabel = new JLabel("Servidor corriendo en " + HOST + " : " + PORT);
            statusLabel.setHorizontalAlignment(JLabel.CENTER);
            getContentPane().add(statusLabel, BorderLayout.CENTER);
        }
    }

    /**
     * La clase Handler maneja las conexiones individuales de los clientes,
     * recibe sus mensajes y los retransmite a todos los demás clientes.
     */
    private static class Handler extends Thread {
        private String name;
        private String imagePath;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructor que inicializa un nuevo manejador para el socket dado.
         *
         * @param socket El socket de la conexión del cliente
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Ejecuta el hilo del manejador, gestionando la interacción con un cliente individual.
         */
        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Solicitar un nombre y ruta de imagen a este cliente
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    synchronized (userWriters) {
                        if (!userWriters.containsKey(input.split(" ")[0])) {
                            name = input.split(" ")[0];
                            imagePath = input.split(" ").length > 1 ? input.split(" ")[1] : null;
                            userWriters.put(name, out);
                            userImages.put(name, imagePath);
                            break;
                        } else {
                            out.println("NAME_EXISTS");
                        }
                    }
                }

                out.println("NAME_ACCEPTED");
                clientWriters.add(out);

                // Notificar a todos los clientes sobre el nuevo usuario
                broadcastUserList();

                // Aceptar mensajes de este cliente y retransmitirlos
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    if (input.startsWith("@")) {
                        String targetUser = input.split(" ")[0].substring(1);
                        String message = input.substring(targetUser.length() + 2);
                        PrintWriter targetWriter = userWriters.get(targetUser);
                        if (targetWriter != null) {
                            targetWriter.println("MESSAGE " + name + ": " + message);
                        }
                    } else {
                        for (PrintWriter writer : clientWriters) {
                            writer.println("MESSAGE " + name + ": " + input);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (name != null) {
                    userWriters.remove(name);
                    userImages.remove(name);
                    clientWriters.remove(out);
                    broadcastUserList();
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Envía la lista de usuarios actual a todos los clientes conectados.
         */
        private void broadcastUserList() {
            StringBuilder userList = new StringBuilder("USER_LIST");
            for (String user : userWriters.keySet()) {
                userList.append(" ").append(user);
                String imagePath = userImages.get(user);
                if (imagePath != null) {
                    userList.append(" ").append(imagePath);
                }
            }
            for (PrintWriter writer : clientWriters) {
                writer.println(userList.toString());
            }
        }
    }
}
