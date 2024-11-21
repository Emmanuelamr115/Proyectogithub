/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.holagithub;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Equipo 1
 */

public class Servidor {
    private ServerSocket serverSocket; // ServerSocket para escuchar conexiones
    private final List<HiloCliente> clientes; // Lista de clientes conectados
    private static final int PUERTO = 12345; // Puerto del servidor

    public Servidor() {
        clientes = new LinkedList<>();
    }

    // Método para iniciar el servidor
    public void iniciarServidor() {
        try {
            serverSocket = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado en el puerto: " + PUERTO);

            // Escucha conexiones indefinidamente
            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + clienteSocket);

                // Crear un hilo para el cliente y agregarlo a la lista
                HiloCliente hilo = new HiloCliente(clienteSocket, this);
                clientes.add(hilo);
                hilo.start();
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    // Método para desconectar un cliente
    public void desconectarCliente(HiloCliente cliente) {
        clientes.remove(cliente);
        System.out.println("Cliente desconectado: " + cliente.getNombre());
    }

    // Método para enviar un mensaje a todos los clientes conectados
    public void enviarMensajeATodos(String mensaje) {
        for (HiloCliente cliente : clientes) {
            cliente.enviarMensaje(mensaje);
        }
    }

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciarServidor();
    }
}
