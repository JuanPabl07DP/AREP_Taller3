package co.edu.escuelaing.arem.ASE;

import co.edu.escuelaing.arem.ASE.annotations.RestController;
import co.edu.escuelaing.arem.ASE.annotations.GetMapping;
import co.edu.escuelaing.arem.ASE.annotations.RequestParam;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MicroSpringBoot {
    private static final Map<String, Method> endpoints = new HashMap<>();
    private static final Map<String, Object> components = new HashMap<>();
    private static final Logger logger = Logger.getLogger(MicroSpringBoot.class.getName());
    private static final int DEFAULT_PORT = 8080;
    private static String staticFilesPath = "target/classes/public";

    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                // Si se proporciona una clase específica, la registramos
                Class<?> controllerClass = Class.forName(args[0]);
                registerController(controllerClass);
            } else {
                // Si no, escaneamos todo el paquete base
                scanComponents("co.edu.escuelaing.arem.ASE");
            }

            startServer(DEFAULT_PORT);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error iniciando el servidor", e);
        }
    }

    private static void scanComponents(String basePackage) {
        try {
            logger.info("Escaneando componentes en el paquete: " + basePackage);
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = basePackage.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File directory = new File(resource.getFile());
                if (directory.exists()) {
                    scanDirectory(directory, basePackage);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error escaneando componentes", e);
        }
    }

    private static void scanDirectory(File directory, String packageName) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, packageName + "." + file.getName());
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + "." +
                            file.getName().substring(0, file.getName().length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(RestController.class)) {
                            logger.info("Encontrado controlador: " + className);
                            registerController(clazz);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error cargando clase: " + className, e);
                    }
                }
            }
        }
    }

    private static void registerController(Class<?> controllerClass) {
        try {
            if (controllerClass.isAnnotationPresent(RestController.class)) {
                logger.info("Registrando controlador: " + controllerClass.getName());
                Object controller = controllerClass.getDeclaredConstructor().newInstance();
                components.put(controllerClass.getName(), controller);

                for (Method method : controllerClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GetMapping.class)) {
                        GetMapping mapping = method.getAnnotation(GetMapping.class);
                        endpoints.put(mapping.value(), method);
                        logger.info("Registrado endpoint: " + mapping.value());
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error registrando controlador", e);
        }
    }

    private static void startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", MicroSpringBoot::handleRequest);
        server.setExecutor(null);
        server.start();
        logger.info("Servidor iniciado en puerto " + port);
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            logger.info("Recibida petición: " + method + " " + path);

            if ("GET".equals(method)) {
                handleGetRequest(exchange, path);
            } else {
                sendMethodNotAllowed(exchange);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error procesando petición", e);
            sendErrorResponse(exchange, "Internal Server Error");
        } finally {
            exchange.close();
        }
    }

    private static void handleGetRequest(HttpExchange exchange, String path) throws IOException {
        Method endpoint = endpoints.get(path);

        if (endpoint != null) {
            handleEndpoint(exchange, endpoint);
        } else {
            serveStaticFile(exchange, path);
        }
    }

    private static void handleEndpoint(HttpExchange exchange, Method endpoint) throws IOException {
        try {
            Object controller = components.get(endpoint.getDeclaringClass().getName());
            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            Object[] args = processParameters(endpoint, queryParams);

            Object result = endpoint.invoke(controller, args);
            sendResponse(exchange, result.toString(), "application/json");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error ejecutando endpoint", e);
            sendErrorResponse(exchange, "Error procesando la solicitud");
        }
    }

    private static Object[] processParameters(Method method, Map<String, String> queryParams) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            RequestParam param = parameters[i].getAnnotation(RequestParam.class);
            if (param != null) {
                String value = queryParams.getOrDefault(param.value(), param.defaultValue());
                args[i] = value;
            }
        }

        return args;
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    params.put(pair[0], pair[1]);
                }
            }
        }
        return params;
    }

    private static void serveStaticFile(HttpExchange exchange, String path) throws IOException {
        path = path.equals("/") ? "/index.html" : path;
        Path filePath = Path.of(staticFilesPath + path);

        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            byte[] fileBytes = Files.readAllBytes(filePath);
            sendResponse(exchange, fileBytes, getContentType(path));
        } else {
            sendNotFound(exchange);
        }
    }

    private static void sendResponse(HttpExchange exchange, String response, String contentType) throws IOException {
        byte[] responseBytes = response.getBytes();
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private static void sendResponse(HttpExchange exchange, byte[] response, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    private static void sendErrorResponse(HttpExchange exchange, String message) throws IOException {
        String response = "{\"error\": \"" + message + "\"}";
        sendResponse(exchange, response, "application/json");
    }

    private static void sendNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, -1);
    }

    private static void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(405, -1);
    }

    private static String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".webp")) return "image/webp";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "text/plain";
    }
}