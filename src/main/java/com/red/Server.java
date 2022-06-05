package com.red;

import java.io.*;
import java.net.*;
import java.util.*;


public class Server {

    static final int BUFFER_SIZE = 15;
    static final int NUM_COMEDIANS = 5;
    static final int SOPHISTICATION = 50000;
    static String mode = new String();
    static BoundedBuffer<String> buffer = new BoundedBuffer<String>(BUFFER_SIZE);
    static BoundedBuffer<String> data = new BoundedBuffer<String>(10);


    /**
     * It receives a request from the client, processes it and sends a response
     *
     * @param socket the socket to which the server is connected
     * @return a thread that is responsible for receiving and processing requests from the client.
     */
    private static Thread get_joke_new(DatagramSocket socket) {
        mode = "choice";
        Thread get_joke = new Thread() {
            @Override
            public void run() {
                boolean state = true;
                try {
                    while (state) {

                        byte[] buf = new byte[256];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        InetAddress address = packet.getAddress();
                        int port = packet.getPort();
                        packet = new DatagramPacket(buf, buf.length, address, port);
                        String received = new String(packet.getData(), 0, packet.getLength());
                        received = received.replaceAll("[^A-Za-z0-9]", "");
                        System.out.println("Received request: " + received + " ,mode - " + mode);
                        String resp = new String();

                        switch (mode){
                            case "choice":
                                if(received.equals("default")||received.equals("fresh")||received.equals("greedy")) {
                                    mode = received;
                                    resp = "The server is connected in the " + mode + " mode";
                                }
                                else resp = "There's no such mode";
                                break;
                            case "fresh":
                                if (received.equals("get")) resp = buffer.take_newer();
                                else if (received.equals("end")) {
                                    state = false;
                                    continue;
                                } else resp = "There's no such request - (" + received + ")";
                                break;
                            case "default":
                                if (received.equals("get"))  resp = buffer.take();
                                else if (received.equals("end")) {
                                    state = false;
                                    continue;
                                } else resp = "There's no such request - (" + received + ")";
                                break;
                            case "greedy":
                                if (received.equals("get"))  resp = buffer.take();
                                break;
                        }

                            DatagramPacket pack = new DatagramPacket(resp.getBytes(), resp.getBytes().length, address, port);
                            socket.send(pack);
                            System.out.println("Response to get request: " + resp);
                    }
                    socket.close();
                } catch (IOException io) {}
            }
        };
        return get_joke;
    }

    /**
     * The function creates a new Runnable object that takes a joke from the data buffer, puts it in the joke buffer, and
     * then prints out the joke
     *
     * @return A Runnable object.
     */
    private static Runnable make_joke() {
        Runnable make_joke = new Runnable() {
            @Override
            public void run() {
                Random random = new Random();
                while (true) {
                    synchronized (Thread.currentThread()){
                        try {
                            Thread.currentThread().wait(random.nextInt(SOPHISTICATION));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    String joke = data.take();
                    buffer.put(joke);
                    System.out.println(Thread.currentThread().getName() + " came up with a joke: " + joke);

                }
            }
        };
        return make_joke;
    }

    /**
     * It creates an array of threads, each of which will run the same Runnable object
     *
     * @param make_joke a Runnable object that will be used to create the threads.
     * @return An array of Threads.
     */
    private static Thread[] comedians_init(Runnable make_joke){
        Thread[] comedians = new Thread[NUM_COMEDIANS];
        for(int i=0;i<NUM_COMEDIANS;i++) comedians[i] = new Thread(make_joke);
        return comedians;
    }

    /**
     * It reads the jokes.txt file and puts the jokes into the data queue.
     *
     * @return A thread that reads the jokes.txt file and puts the jokes into the data queue.
     */
    private static Thread data_init() throws FileNotFoundException  {
        BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/jokes.txt"));
        Thread init_data = new Thread(){
            @Override
            public void run() {
                String line;
                    try {
                        while(true) {
                            if ((line = reader.readLine()) != null) {
                                data.put(line);
                               // System.out.println("Collected " + line);
                            } else {
                                reader.close();
                                break;
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

            }
        };
        return init_data;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 4445;

        DatagramSocket socket = new DatagramSocket(port);
        Thread server = get_joke_new(socket);

        Thread data = data_init();
        data.start();
        Thread[] comedians = comedians_init(make_joke());

        for(Thread i : comedians){
            i.start();
        }
        server.start();
        System.out.println("Server has started at localhost, port - " + port);
    }

}





