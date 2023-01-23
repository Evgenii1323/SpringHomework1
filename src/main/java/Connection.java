import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

public class Connection implements Runnable {
    private final BufferedReader IN;
    private final BufferedOutputStream OUT;
    private final List<String> VALID_PATHS = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public Connection(Socket socket) throws IOException {
        this.IN = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.OUT = new BufferedOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        while (true) {
            try {
                String requestLine = IN.readLine();
                String[] parts = requestLine.split(" ");
                if (parts.length != 3) {
                    break;
                }
                String path = parts[1];

                if (!VALID_PATHS.contains(path)) {
                    OUT.write((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    OUT.flush();
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

    public void writeClassicHtml(Path filePath, String mimeType) throws IOException {
        String template = Files.readString(filePath);
        byte[] content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();
        OUT.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        OUT.write(content);
        OUT.flush();
    }

    public void write(Path filePath, String mimeType, long length) throws IOException {
        OUT.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, OUT);
        OUT.flush();
    }
}