import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Server {
    public static void main(String[] args) throws IOException {
        try (ServerSocket ss = new ServerSocket(8000)) {
            System.out.println("server is running...");
            while (true) {
                Socket socket = ss.accept();
                System.out.println("client connected" + socket.getRemoteSocketAddress());
                Thread t = new Handler(socket);
                t.start();
            }
        }
    }
}

class Handler extends Thread {
    Socket socket;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (InputStream inputStream = this.socket.getInputStream()) {
            try (OutputStream outputStream = this.socket.getOutputStream()) {
                handle(inputStream, outputStream);
            }
        } catch (Exception e) {
        } finally {
            try {
                this.socket.close();
            } catch (IOException e) {
            }
            System.out.println("client disconnected" + socket.getRemoteSocketAddress());
        }
    }

    private void handle(InputStream inputStream, OutputStream outputStream) throws IOException {
        System.out.println("handle http request...");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        boolean ok = false;
        String firstLine = reader.readLine();
        if (firstLine.startsWith("GET / HTTP/1.")) {
            ok = true;
        }

        while (true) {
            String header = reader.readLine();
            if (header.isEmpty()) {
                break;
            }
            System.out.println(header);
        }
        System.out.println(ok? "Response ok!" : "Response error!");
        if (!ok) {
            writer.write("HTTP/1.0 404 Not Found\r\n");
            writer.write("Content-Length: 0\r\n");
            writer.write("\r\n");
            writer.flush();
        } else {
            String data = "<html><body><h1>Request success!</h1></body></html>";
            int length = data.getBytes(StandardCharsets.UTF_8).length;
            writer.write("HTTP/1.0 200 OK\r\n");
            writer.write("Connection: close\r\n");
            writer.write("Content-Type: text/html\r\n");
            writer.write("Content-Length: " + length + "\r\n");
            writer.write("\r\n");
            writer.write(data);
            writer.flush();
        }
    }
}


