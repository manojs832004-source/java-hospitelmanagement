import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {
    private static final int PORT = 8080;
    private static final String BASE_PATH = "./";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        
        // Create context for serving static files
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/", new ApiHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("========================================");
        System.out.println("🏥 Doctor Appointment System - Web Server");
        System.out.println("========================================");
        System.out.println("Server started on: http://localhost:" + PORT);
        System.out.println("Open your browser and navigate to the URL above");
        System.out.println("Press Ctrl+C to stop the server");
        System.out.println("========================================");
    }

    // Handler for static files (HTML, CSS, JS)
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Default to index.html for root path
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            // Remove leading slash for file path
            String filePath = BASE_PATH + path.substring(1);
            File file = new File(filePath);
            
            // Security check - prevent directory traversal
            if (!file.getCanonicalPath().startsWith(new File(BASE_PATH).getCanonicalPath())) {
                sendResponse(exchange, 403, "Forbidden");
                return;
            }
            
            // Check if file exists
            if (!file.exists() || file.isDirectory()) {
                sendResponse(exchange, 404, "Not Found - File: " + filePath);
                return;
            }
            
            // Read and serve file
            try {
                byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
                
                // Set content type based on file extension
                String contentType = getContentType(filePath);
                
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, fileContent.length);
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(fileContent);
                }
            } catch (IOException e) {
                sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        }
        
        private String getContentType(String filePath) {
            if (filePath.endsWith(".html")) return "text/html";
            if (filePath.endsWith(".css")) return "text/css";
            if (filePath.endsWith(".js")) return "application/javascript";
            if (filePath.endsWith(".json")) return "application/json";
            if (filePath.endsWith(".png")) return "image/png";
            if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) return "image/jpeg";
            if (filePath.endsWith(".gif")) return "image/gif";
            if (filePath.endsWith(".svg")) return "image/svg+xml";
            return "text/plain";
        }
    }

    // Handler for API requests (will connect to Java backend)
    static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            
            // Enable CORS
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            
            if (method.equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                // Doctors API
                if (path.equals("/api/doctors/list")) {
                    handleGetDoctors(exchange);
                }
                else if (path.equals("/api/doctors/register")) {
                    handleDoctorRegister(exchange);
                }
                else if (path.equals("/api/doctors/login")) {
                    handleDoctorLogin(exchange);
                }
                // Patients API
                else if (path.equals("/api/patients/register")) {
                    handlePatientRegister(exchange);
                }
                else if (path.equals("/api/patients/login")) {
                    handlePatientLogin(exchange);
                }
                // Appointment Requests
                else if (path.equals("/api/appointment/create")) {
                    handleCreateAppointmentRequest(exchange);
                }
                else if (path.equals("/api/appointment/requests/patient")) {
                    handleGetPatientRequests(exchange);
                }
                else if (path.equals("/api/appointment/requests/doctor")) {
                    handleGetDoctorRequests(exchange);
                }
                else if (path.equals("/api/appointment/accept")) {
                    handleAcceptRequest(exchange);
                }
                else if (path.equals("/api/appointment/reject")) {
                    handleRejectRequest(exchange);
                }
                // Appointments
                else if (path.equals("/api/appointments/patient")) {
                    handleGetPatientAppointments(exchange);
                }
                else if (path.equals("/api/appointments/doctor")) {
                    handleGetDoctorAppointments(exchange);
                }
                else {
                    sendJsonResponse(exchange, 404, "{\"error\": \"Endpoint not found\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"error\": \"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
        
        private void handleGetDoctors(HttpExchange exchange) throws IOException {
            StringBuilder json = new StringBuilder("[");
            java.util.List<AppointmentManager.Doctor> doctors = AppointmentManager.getAllDoctors();
            for (int i = 0; i < doctors.size(); i++) {
                AppointmentManager.Doctor doc = doctors.get(i);
                if (i > 0) json.append(",");
                json.append(String.format(
                    "{\"id\":\"%s\",\"name\":\"%s\",\"specialty\":\"%s\"}",
                    doc.id, doc.name, doc.specialty
                ));
            }
            json.append("]");
            sendJsonResponse(exchange, 200, json.toString());
        }
        
        private void handleDoctorRegister(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            try {
                java.util.Map<String, String> params = parseJson(body);
                String doctorId = params.get("doctorId");
                String name = params.get("name");
                String specialty = params.get("specialty");
                String password = params.get("password");
                
                if (AppointmentManager.registerDoctor(doctorId, name, specialty, password)) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Doctor registered successfully\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Doctor ID already exists\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid request: " + escapeJson(e.getMessage()) + "\"}");
            }
        }
        
        private void handleDoctorLogin(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            try {
                java.util.Map<String, String> params = parseJson(body);
                String doctorId = params.get("doctorId");
                String password = params.get("password");
                
                AppointmentManager.Doctor doc = AppointmentManager.loginDoctor(doctorId, password);
                if (doc != null) {
                    String response = String.format(
                        "{\"success\":true,\"doctorId\":\"%s\",\"name\":\"%s\",\"specialty\":\"%s\"}",
                        doc.id, doc.name, doc.specialty
                    );
                    sendJsonResponse(exchange, 200, response);
                } else {
                    sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"Invalid credentials\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
            }
        }
        
        private void handlePatientRegister(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            try {
                java.util.Map<String, String> params = parseJson(body);
                String patientId = params.get("patientId");
                String name = params.get("name");
                int age = Integer.parseInt(params.get("age"));
                String password = params.get("password");
                
                if (AppointmentManager.registerPatient(patientId, name, age, password)) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Patient registered successfully\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Patient ID already exists\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid request: " + escapeJson(e.getMessage()) + "\"}");
            }
        }
        
        private void handlePatientLogin(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            try {
                java.util.Map<String, String> params = parseJson(body);
                String patientId = params.get("patientId");
                String password = params.get("password");
                
                AppointmentManager.Patient pat = AppointmentManager.loginPatient(patientId, password);
                if (pat != null) {
                    String response = String.format(
                        "{\"success\":true,\"patientId\":\"%s\",\"name\":\"%s\",\"age\":%d}",
                        pat.id, pat.name, pat.age
                    );
                    sendJsonResponse(exchange, 200, response);
                } else {
                    sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"Invalid credentials\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
            }
        }

        private void handleCreateAppointmentRequest(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            try {
                java.util.Map<String, String> params = parseJson(body);
                String patientId = params.get("patientId");
                String patientName = params.get("patientName");
                String doctorId = params.get("doctorId");
                String date = params.get("date");
                String time = params.get("time");
                String reason = params.get("reason");
                
                AppointmentManager.AppointmentRequest req = AppointmentManager.createRequest(
                    patientId, patientName, doctorId, date, time, reason
                );
                
                String response = String.format(
                    "{\"success\":true,\"requestId\":\"%s\",\"message\":\"Request sent successfully\"}",
                    req.id
                );
                sendJsonResponse(exchange, 200, response);
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
            }
        }
        
        private void handleGetPatientRequests(HttpExchange exchange) throws IOException {
            try {
                String query = exchange.getRequestURI().getQuery();
                String patientId = extractParamValue(query, "patientId");
                
                StringBuilder json = new StringBuilder("[");
                java.util.List<AppointmentManager.AppointmentRequest> reqs = 
                    AppointmentManager.getRequestsByPatient(patientId);
                    
                for (int i = 0; i < reqs.size(); i++) {
                    AppointmentManager.AppointmentRequest req = reqs.get(i);
                    if (i > 0) json.append(",");
                    AppointmentManager.Doctor doc = AppointmentManager.getDoctorById(req.doctorId);
                    json.append(String.format(
                        "{\"id\":\"%s\",\"doctorName\":\"%s\",\"doctorId\":\"%s\",\"date\":\"%s\",\"time\":\"%s\",\"reason\":\"%s\",\"status\":\"%s\"}",
                        req.id, doc != null ? doc.name : "Unknown", req.doctorId, req.date, req.time,
                        escapeJson(req.reason), req.status
                    ));
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
            }
        }
        
        private void handleGetDoctorRequests(HttpExchange exchange) throws IOException {
            try {
                String query = exchange.getRequestURI().getQuery();
                String doctorId = extractParamValue(query, "doctorId");
                
                StringBuilder json = new StringBuilder("[");
                java.util.List<AppointmentManager.AppointmentRequest> reqs = 
                    AppointmentManager.getRequestsForDoctor(doctorId);
                    
                for (int i = 0; i < reqs.size(); i++) {
                    AppointmentManager.AppointmentRequest req = reqs.get(i);
                    if (i > 0) json.append(",");
                    json.append(String.format(
                        "{\"id\":\"%s\",\"patientName\":\"%s\",\"patientId\":\"%s\",\"date\":\"%s\",\"time\":\"%s\",\"reason\":\"%s\",\"status\":\"%s\"}",
                        req.id, req.patientName, req.patientId, req.date, req.time,
                        escapeJson(req.reason), req.status
                    ));
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
            }
        }
        
        private void handleAcceptRequest(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            try {
                java.util.Map<String, String> params = parseJson(body);
                String requestId = params.get("requestId");
                
                if (AppointmentManager.acceptRequest(requestId)) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Request accepted\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Request not found\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
            }
        }
        
        private void handleRejectRequest(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            try {
                java.util.Map<String, String> params = parseJson(body);
                String requestId = params.get("requestId");
                
                if (AppointmentManager.rejectRequest(requestId)) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Request rejected\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Request not found\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
            }
        }
        
        private void handleGetPatientAppointments(HttpExchange exchange) throws IOException {
            try {
                String query = exchange.getRequestURI().getQuery();
                String patientId = extractParamValue(query, "patientId");
                
                StringBuilder json = new StringBuilder("[");
                java.util.List<AppointmentManager.Appointment> apts = 
                    AppointmentManager.getAppointmentsForPatient(patientId);
                    
                for (int i = 0; i < apts.size(); i++) {
                    AppointmentManager.Appointment apt = apts.get(i);
                    if (i > 0) json.append(",");
                    AppointmentManager.Doctor doc = AppointmentManager.getDoctorById(apt.doctorId);
                    json.append(String.format(
                        "{\"id\":\"%s\",\"doctorName\":\"%s\",\"date\":\"%s\",\"time\":\"%s\",\"status\":\"%s\"}",
                        apt.id, doc != null ? doc.name : "Unknown", apt.date, apt.time, apt.status
                    ));
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
            }
        }
        
        private void handleGetDoctorAppointments(HttpExchange exchange) throws IOException {
            try {
                String query = exchange.getRequestURI().getQuery();
                String doctorId = extractParamValue(query, "doctorId");
                
                StringBuilder json = new StringBuilder("[");
                java.util.List<AppointmentManager.Appointment> apts = 
                    AppointmentManager.getAppointmentsForDoctor(doctorId);
                    
                for (int i = 0; i < apts.size(); i++) {
                    AppointmentManager.Appointment apt = apts.get(i);
                    if (i > 0) json.append(",");
                    json.append(String.format(
                        "{\"id\":\"%s\",\"patientName\":\"%s\",\"date\":\"%s\",\"time\":\"%s\",\"status\":\"%s\"}",
                        apt.id, apt.patientName, apt.date, apt.time, apt.status
                    ));
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
            }
        }
        
        private String readRequestBody(HttpExchange exchange) throws IOException {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody())
            );
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        }
        
        private java.util.Map<String, String> parseJson(String json) {
            java.util.Map<String, String> map = new java.util.HashMap<>();
            try {
                json = json.trim();
                if (!json.startsWith("{") || !json.endsWith("}")) {
                    return map;
                }
                
                // Remove braces
                json = json.substring(1, json.length() - 1).trim();
                if (json.isEmpty()) {
                    return map;
                }
                
                // Split by comma, but need to handle escaped quotes
                String[] parts = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                
                for (String part : parts) {
                    part = part.trim();
                    int colonIndex = part.indexOf(':');
                    if (colonIndex > 0) {
                        String key = part.substring(0, colonIndex).trim();
                        String value = part.substring(colonIndex + 1).trim();
                        
                        // Remove quotes from key
                        if (key.startsWith("\"") && key.endsWith("\"")) {
                            key = key.substring(1, key.length() - 1);
                        }
                        
                        // Remove quotes from value if it's a string
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                            // Unescape quotes and special chars
                            value = value.replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r");
                        }
                        
                        map.put(key, value);
                    }
                }
            } catch (Exception e) {
                map.clear();
            }
            return map;
        }
        
        private String extractParamValue(String queryString, String paramName) {
            if (queryString == null || queryString.isEmpty()) return "";
            try {
                String[] params = queryString.split("&");
                for (String param : params) {
                    String[] kv = param.split("=", 2);
                    if (kv.length == 2 && kv[0].equals(paramName)) {
                        return java.net.URLDecoder.decode(kv[1], "UTF-8");
                    }
                }
            } catch (Exception e) {
                // Return empty string on decode error
            }
            return "";
        }
        
        private String escapeJson(String str) {
            if (str == null) return "";
            return str.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        }
    }

    // Helper method to send JSON response
    static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = json.getBytes();
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    // Helper method to send text response
    static void sendResponse(HttpExchange exchange, int statusCode, String text) throws IOException {
        byte[] responseBytes = text.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
