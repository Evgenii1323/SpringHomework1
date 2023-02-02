import java.io.*;

public class Main {
    private static final int NUMBER_OF_THREADS = 64;
    private static final int PORT = 9999;

    public static void main(String[] args) {
        Server server = new Server(NUMBER_OF_THREADS, PORT);
        server.addHandler("GET", "/messages", (Request request, BufferedOutputStream responseStream) -> {
            try {
                String message = "Hello";
                responseStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + "text/plain" + "\r\n" +
                                "Content-Length: " + message.length() + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                responseStream.write(message.getBytes());
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.start();
    }
}