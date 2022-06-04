package com.red;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;


public class Server {

    static final int BUFFER_SIZE = 5;
    static final int NUM_COMEDIANS = 5;
    static final int SOPHISTICATION = 500;
    static BoundedBuffer<String> buffer = new BoundedBuffer<String>(BUFFER_SIZE);
    static BoundedBuffer<String> data = new BoundedBuffer<String>(10);


    private static Thread get_joke(Socket sock) {
        Thread get_joke = new Thread() {
            @Override
            public void run() {
                PrintStream out;
                BufferedReader in;

                try {
                    in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    out = new PrintStream(sock.getOutputStream());
                    out.println("get");                        //send client the mode
                    out.flush();

                    String temp = buffer.take();

                    out.println(temp);                            //send client the content
                    out.flush();

                    System.out.println("Content " + temp + " sent");

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        };
        return get_joke;
    }


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
                    String temp = data.take();
                    buffer.put(temp);
                    System.out.println("Thread - " + Thread.currentThread().getName() + " Response: " + temp);

                }
            }
        };
        return make_joke;
    }

    private static Thread[] comedians_init(Runnable make_joke){
        Thread[] comedians = new Thread[NUM_COMEDIANS];
        for(int i=0;i<NUM_COMEDIANS;i++) comedians[i] = new Thread(make_joke);
        return comedians;
    }

    private static Thread data_init() throws FileNotFoundException  {
        BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/jokes.txt"));
        Thread init_data = new Thread(){
            @Override
            public void run() {
                String line;
                while(true){
                    try {
                        if ((line = reader.readLine())!=null) {
                         data.put(line);
                         System.out.println("Collected " + line);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    ;
                }
            }
        };
        return init_data;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int q_len = 6;
        int port = 4546;
        Socket s;

        ServerSocket ss = new ServerSocket(port,q_len);

        Thread data = data_init();
        data.start();
        Thread[] comedians = comedians_init(make_joke());

//        Thread joke = new Thread(make_joke());
//        joke.start();

        for(Thread i : comedians){
            i.start();
        }

        s = ss.accept();
        get_joke(s).start();

    }

}





