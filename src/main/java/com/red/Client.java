package com.red;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;


public class Client{

    private static byte[] buf;

    /**
     * It takes a string, converts it to a byte array, sends it to the server, waits for a response, converts the response
     * to a string, and returns it
     *
     * @param msg The message to send to the server
     * @param address The address of the server
     * @param socket The socket that will be used to send and receive the data.
     * @return The message that was sent to the server.
     */
    public static String sendEcho(String msg,InetAddress address, DatagramSocket socket) throws IOException {
        buf = msg.getBytes();
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, address, 4445);
        socket.send(packet);
        byte[] buff = new byte[256];
        packet = new DatagramPacket(buff, buff.length);
        socket.receive(packet);
        String received = new String(packet.getData(), 0, packet.getLength());
        return received;
    }

    /**
     * Closes the socket.
     *
     * @param socket The socket to close.
     */
    public static void close(DatagramSocket socket) {
        socket.close();
    }


    public static void main(String[] args) throws IOException {
        InetAddress address = InetAddress.getByName("localhost");
        DatagramSocket socket = new DatagramSocket();
        Scanner scanner = new Scanner(System.in);
        String mode = new String();
        System.out.println("Client access to the joke server");
        while(true){
            System.out.println("Chose the mode of the server:\nfresh - always take the most recently produced joke\ngreedy = take all jokes as they are produced\ndefault - take whatever joke that has not been received yet");
            System.out.println("Enter the mode: ");
            mode = scanner.nextLine();
            String resp = sendEcho(mode,address,socket);
            System.out.println(resp);
            if(!resp.equals("There's no such mode")) break;
        }

        if(!mode.equals("greedy")){
        System.out.println("Possible requests:\nget - get a joke\nend - exit");
        while (true){
            System.out.println("Enter the request: ");
            String massage = scanner.nextLine();

            System.out.println("Joke - " + sendEcho(massage,address,socket));

            if(massage.equals("end")) {
                break;
            }
        }} else {
            while (true){
                System.out.println("Joke - " + sendEcho("get",address,socket));
            }
        }

        close(socket);
    }
}

