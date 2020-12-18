package ru.spbau.javacourse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class FTPServer
{
    public static final int PORT = 4444;
    private static Logger log = Logger.getLogger(FTPServer.class.getName());

    public static class MasterThread implements Runnable {
        CountDownLatch start;
        AtomicBoolean stopFlag;
        ServerSocket serverSocket;
        public MasterThread(ServerSocket serverSocket, CountDownLatch start, AtomicBoolean stopFlag) {
            this.start = start;
            this.stopFlag = stopFlag;
            this.serverSocket = serverSocket;
        }
        public void run() {
            try {
                start.await();
                log.info("Starting server...");
                while (!stopFlag.get()) {
                    log.info("Waiting to someone connects");
                    Socket clientSocket = serverSocket.accept();
                    Thread worker = new Thread(new ServerWorker(clientSocket));
                    worker.setDaemon(true);
                    worker.start();
                }
            } catch (InterruptedException | IOException e) {
                log.info("Stopping server...");
            }
        }
    }

    public static void main(String[] args) {
        CountDownLatch start = new CountDownLatch(1);
        AtomicBoolean stopFlag = new AtomicBoolean(false);
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            Thread masterThread = new Thread(new MasterThread(serverSocket, start, stopFlag));
            masterThread.start();
            while(!stopFlag.get()) {
                String cmd = console.readLine();
                if(cmd.equalsIgnoreCase("start")) {
                    start.countDown();
                } else if(cmd.equalsIgnoreCase("stop")) {
                    stopFlag.set(true);
                    serverSocket.close();
                } else {
                    System.out.println("Invalid command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
