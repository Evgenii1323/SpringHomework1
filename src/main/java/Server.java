import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server extends Thread {
    private final int numberOfThreads;
    private final int port;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server(int numberOfThreads, int port) {
        this.numberOfThreads = numberOfThreads;
        this.port = port;
    }

    @Override
    public void run() {
        ExecutorService threadPool = Executors.newFixedThreadPool(numberOfThreads);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    Connection connection = new Connection(handlers, socket);
                    threadPool.submit(connection);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        if (!handlers.containsKey(method)) {
            handlers.put(method, new ConcurrentHashMap<>());
        }

        ConcurrentHashMap<String, Handler> map = handlers.get(method);
        map.put(path, handler);
    }
}