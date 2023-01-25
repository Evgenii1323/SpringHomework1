import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

public class Connection implements Runnable {
    private final Socket socket;
    private static final List<String> VALID_PATHS = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public Connection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        while (true) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
                String requestLine = in.readLine();
                String[] parts = requestLine.split(" ");

                if (parts.length != 3) {
                    break;
                }

                String path = parts[1];

                if (!VALID_PATHS.contains(path)) {
                    out.write((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                    break;
                }

                Path filePath = Path.of(".", "public", path);
                String mimeType = Files.probeContentType(filePath);
                long length = Files.size(filePath);

                if (path.equals("/classic.html")) {
                    writeClassicHtml(filePath, mimeType);
                } else {
                    write(filePath, mimeType, length);
                }
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeClassicHtml(Path filePath, String mimeType) {
        try (BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
            String template = Files.readString(filePath);
            byte[] content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();

            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(Path filePath, String mimeType, long length) {
        try (BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}