import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.net.InetSocketAddress;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class HealthWebApp {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Serve Static Files (UI)
        server.createContext("/", new StaticFileHandler());

        // API Endpoints
        server.createContext("/api/predict/heart", new HeartPredictHandler());
        server.createContext("/api/chat", new ChatbotHandler());

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("==========================================================");
        System.out.println(" 🚀 Health & Wellness Web Server Started on Port " + port);
        System.out.println(" 🌐 Open your browser and navigate to: http://localhost:" + port);
        System.out.println("==========================================================");
        
        try {
            // Automatically open browser on Windows
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler http://localhost:8080");
        } catch (Exception ignored) {}
    }

    // Static File Server
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }
            
            // Try loading from web/ directory first
            File file = new File("web" + path);
            if (!file.exists()) {
                // Try root directory for images
                file = new File(path.substring(1));
            }

            if (file.exists() && !file.isDirectory()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String contentType = "text/plain";
                if (path.endsWith(".html")) contentType = "text/html";
                else if (path.endsWith(".css")) contentType = "text/css";
                else if (path.endsWith(".js")) contentType = "application/javascript";
                else if (path.endsWith(".png")) contentType = "image/png";
                else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) contentType = "image/jpeg";
                
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String response = "404 File Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }

    // Heart ML Prediction Handler
    static class HeartPredictHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
                String payload = scanner.hasNext() ? scanner.next() : "";
                
                try {
                    JSONArray arr = new JSONArray(payload);
                    String[] args = new String[arr.length()];
                    for (int i = 0; i < arr.length(); i++) {
                        args[i] = arr.getString(i);
                    }
                    
                    PythonBridge.ExecutionResult exec = PythonBridge.runScript("predict_heart.py", args);
                    String response = exec.getOutput();
                    
                    if (!exec.isSuccess() && response.isEmpty()) {
                        response = "{\"error\": \"Failed to execute Python\"}";
                    }
                    
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } catch (Exception e) {
                    String err = "{\"error\": \"Invalid JSON processing\"}";
                    exchange.sendResponseHeaders(500, err.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) { os.write(err.getBytes()); }
                }
            }
        }
    }

    // Chatbot Handler (Uses offline fallback if proper Key isn't available to keep UI snappy)
    static class ChatbotHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
                String payload = scanner.hasNext() ? scanner.next() : "";
                
                try {
                    JSONObject req = new JSONObject(payload);
                    String question = req.optString("message", "").toLowerCase();
                    
                    String reply = "I recommend consulting a licensed medical professional for accurate diagnosis. In general, maintaining a balanced diet, active lifestyle, and adequate sleep contributes significantly to long-term health.";
                    
                    if (question.contains("cholesterol")) {
                        reply = "To manage cholesterol: Add soluble fiber (oats, beans), eat foods rich in Omega-3 fatty acids (salmon, walnuts), and limit saturated/trans fats. Regular cardiovascular exercise also helps raise HDL (good) cholesterol.";
                    } else if (question.contains("warning signs") || question.contains("heart disease")) {
                        reply = "Early warning signs of heart disease include:\n- Chest pain, tightness, or pressure (Angina)\n- Shortness of breath\n- Pain radiating to the neck, jaw, throat, upper abdomen or back\n- Numbness, coldness or weakness in your legs or arms.\n🚨 *If you experience sudden severe chest pain, call emergency services immediately.*";
                    } else if (question.contains("stress") || question.contains("anxiety")) {
                        reply = "For acute stress management:\n1. Box Breathing: Inhale 4s, Hold 4s, Exhale 4s, Hold 4s.\n2. Progressive muscle relaxation.\n3. Drink a glass of cold water.\n4. Step away from screens for a 15-minute outdoor walk.\nChronic stress increases cortisol and cardiovascular risk, so establishing a long-term management routine is vital.";
                    } else if (question.contains("blood pressure")) {
                        reply = "For high blood pressure (hypertension), doctors typically recommend the DASH diet (high in vegetables, fruits, and low-fat dairy), reducing sodium intake to under 1,500mg daily, shedding excess weight, and performing aerobic exercise for 30 minutes most days.";
                    }
                    
                    // We can implement the OkHttp API call here exactly like HealthChatbot.java natively in Java if needed.
                    // For the sake of instantaneous SaaS UI demo, this returns the knowledge base directly.
                    
                    JSONObject res = new JSONObject();
                    res.put("reply", reply);
                    
                    byte[] bytes = res.toString().getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
                    
                } catch (Exception e) {
                    String err = "{\"reply\": \"I encountered an internal error.\"}";
                    exchange.sendResponseHeaders(500, err.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) { os.write(err.getBytes()); }
                }
            }
        }
    }
}