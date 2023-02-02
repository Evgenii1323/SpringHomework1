import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Connection implements Runnable {
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers;
    private final Socket socket;

    public Connection(ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers, Socket socket) {
        this.handlers = handlers;
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
            String requestLine = in.readLine();
            String[] parts = requestLine.split(" ");
            if (parts.length != 3) {
                return;
            }
            Request request = new Request(parts);
            System.out.println("Значение = ");
            request.getQueryParam("value").forEach(System.out::println);
            ConcurrentHashMap<String, Handler> map = handlers.get(request.getMethod());
            if (map == null) {
                sendBadRequest(out);
                return;
            }

            Handler handler = map.get(request.getPath());
            if (handler == null) {
                sendBadRequest(out);
                return;
            }

            handler.handle(request, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendBadRequest(BufferedOutputStream out) {
        try {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}