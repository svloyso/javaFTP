package ru.spbau.javacourse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * Created by svloyso on 18.10.16.
 */
public class FTPTest {
    private final Path serverDir = Paths.get("src/test/server");
    @Before
    public void before() throws IOException {
        if(!Files.exists(serverDir)) {
            Files.createDirectory(serverDir);
        }
        Files.write(serverDir.resolve("test1"), "12345".getBytes());
        Files.write(serverDir.resolve("test2"), "67890".getBytes());
    }
    @Test
    public void ftpTest() throws IOException {
        ServerSocket serverSocket = new ServerSocket(FTPServer.PORT);
        CountDownLatch start = new CountDownLatch(1);
        AtomicBoolean stopFlag = new AtomicBoolean(false);
        Thread masterThread = new Thread(new FTPServer.MasterThread(serverSocket, start, stopFlag));
        masterThread.start();
        start.countDown();

        Socket socket = new Socket("localhost", FTPClient.PORT);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        out.writeInt(1);
        out.writeUTF("src/test/server");

        assertEquals(2, in.readInt());
        assertEquals("test1", in.readUTF());
        assertFalse(in.readBoolean());
        assertEquals("test2", in.readUTF());
        assertFalse(in.readBoolean());

        out.writeInt(2);
        out.writeUTF("src/test/server/test1");
        assertEquals(5, in.readInt());
        byte[] b = new byte[5];
        int s = in.read(b);
        assertEquals(5, s);
        assertArrayEquals(Files.readAllBytes(serverDir.resolve("test1")), b);

        stopFlag.set(true);
        serverSocket.close();
    }
    @After
    public void after() throws IOException {
        Files.deleteIfExists(serverDir.resolve("test1"));
        Files.deleteIfExists(serverDir.resolve("test2"));
        Files.deleteIfExists(serverDir);
    }
}