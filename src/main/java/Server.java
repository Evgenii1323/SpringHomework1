import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    private static final int NUMBER_OF_THREADS = 64;
    private static final int PORT = 9999;

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    Connection connection = new Connection(socket);
                    threadPool.submit(connection);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}