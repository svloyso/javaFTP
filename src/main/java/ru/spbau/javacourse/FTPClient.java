package ru.spbau.javacourse;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Created by svloyso on 18.10.16.
 */
public class FTPClient {
    public static final int PORT = FTPServer.PORT;
    public static Logger log = Logger.getLogger(FTPServer.class.getName());

    public static void main(String[] args) {
        String address;
        if(args.length < 2) {
            //log.severe("Give me an address of a server to connect");
            //System.exit(1);
            address = "localhost";
        } else {
            address = args[1];
        }
        try (Socket socket = new Socket(address, PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in)))
        {
            while(true) {
                String line = console.readLine();
                if(line.equalsIgnoreCase("exit")) {
                    System.out.println("Bye.");
                    System.exit(0);
                }
                int sep = line.indexOf(' ');
                String cmd = line.substring(0, sep == -1 ? line.length() : sep);
                String arg = sep == -1 ? "" : line.substring(sep + 1);
                if(cmd.equalsIgnoreCase("list")) {
                    if(arg.isEmpty()) arg = ".";
                    output.writeInt(1);
                    output.writeUTF(arg);
                    output.flush();
                    int size = input.readInt();
                    System.out.println(size);
                    for(int i = 0; i < size; ++i) {
                        String path = input.readUTF();
                        boolean isDir = input.readBoolean();
                        System.out.println(path + (isDir ? "\tDIR" : ""));
                    }
                } else if (cmd.equalsIgnoreCase("get")) {
                    output.writeInt(2);
                    output.writeUTF(arg);
                    output.flush();
                    int size = input.readInt();
                    System.out.println(size);
                    if(size != 0) {
                        byte[] data = new byte[size];
                        int readed = input.read(data);
                        if(readed != size) {
                            System.out.println("ERROR: declared size and actual received size doesn't match!");
                            continue;
                        }
                        Files.write(Paths.get(arg).getFileName(), data);
                    }
                } else if (cmd.equalsIgnoreCase("cd")) {
                    output.writeInt(3);
                    output.writeUTF(arg);
                    output.flush();
                    int size = input.readInt();
                    System.out.println(size);
                } else {
                    System.out.println("Invalid command. Available commands: list [dir], get <file>, cd <dir>");
                }
            }
        } catch (IOException e) {
            System.out.println("Connection was closed. Exit.");
        }

    }
}
