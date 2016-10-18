package ru.spbau.javacourse;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Created by svloyso on 17.10.16.
 */
public class ServerWorker implements Runnable {
    private Socket socket;
    private Path wd;
    private static Logger log = Logger.getLogger(FTPServer.class.getName());

    public ServerWorker(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        wd = Paths.get(".");
        log.info("Established connection from " + socket.getInetAddress());
        try(DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream()))
        {
            while(true) {
                int cmd = input.readInt();
                String arg = input.readUTF();
                if(cmd == 1) {
                    log.info("Got the list " + arg + " command from " + socket.getInetAddress());
                    Path path = wd.resolve(arg);
                    if(!Files.exists(path) || !Files.isDirectory(path)) {
                        output.writeInt(0);
                    } else {
                        int count = (int)Files.list(path).count();
                        output.writeInt(count);
                        Files.list(path).sorted().forEach(p -> {
                            try {
                                output.writeUTF(p.getFileName().toString());
                                output.writeBoolean(Files.isDirectory(p));
                            } catch (IOException e) {
                                log.warning("Can not access to file " + p.toString());
                            }
                        });
                    }
                } else if (cmd == 2) {
                    log.info("Got the get " + arg + " command from " + socket.getInetAddress());
                    Path path = wd.resolve(arg);
                    if (!Files.exists(path) || Files.isDirectory(path)) {
                        output.writeInt(0);
                    } else {
                        output.writeInt((int) Files.size(path));
                        output.write(Files.readAllBytes(path));
                    }
                } else if (cmd == 3) {
                    log.info("Got the cd " + arg + " command from " + socket.getInetAddress());
                    Path path = wd.resolve(arg);
                    if(!Files.exists(path) || !Files.isDirectory(path)) {
                        output.writeInt(0);
                    } else {
                        output.writeInt(1);
                        wd = path;
                    }
                } else {
                    log.warning("Got an invalid command (" + cmd + ") from " + socket.getInetAddress() + ". Ignoring.");
                }
            }
        } catch (IOException e) {
            log.info("Closed connection " + socket.getInetAddress());
        }
    }
}
