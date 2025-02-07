package co.edu.escuelaing.arem.ASE.service;

import co.edu.escuelaing.arem.ASE.model.Movie;
import com.google.gson.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MovieService {
    private static final String SWAPI_URL = "https://swapi.py4e.com/api/films/?format=json";
    private static final Logger logger = Logger.getLogger(MovieService.class.getName());
    private final HttpClient client;
    private final Gson gson;

    public MovieService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    public Movie getMovieById(String id) throws MovieServiceException {
        try {
            validateId(id);
            int searchId = Integer.parseInt(id);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SWAPI_URL))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Código de respuesta: " + response.statusCode());

            if (response.statusCode() != 200) {
                throw new MovieServiceException("Error del servidor: " + response.statusCode());
            }

            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
            JsonArray results = jsonResponse.getAsJsonArray("results");

            for (JsonElement element : results) {
                JsonObject movieJson = element.getAsJsonObject();
                if (movieJson.has("episode_id")) {
                    int episodeId = movieJson.get("episode_id").getAsInt();
                    if (episodeId == searchId) {
                        JsonObject filteredMovie = new JsonObject();
                        filteredMovie.addProperty("title", movieJson.get("title").getAsString());
                        filteredMovie.addProperty("episode_id", movieJson.get("episode_id").getAsInt());
                        filteredMovie.addProperty("opening_crawl", movieJson.get("opening_crawl").getAsString());
                        filteredMovie.addProperty("director", movieJson.get("director").getAsString());
                        filteredMovie.addProperty("producer", movieJson.get("producer").getAsString());
                        filteredMovie.addProperty("release_date", movieJson.get("release_date").getAsString());

                        logger.info("Respuesta filtrada: " + gson.toJson(filteredMovie));

                        return gson.fromJson(filteredMovie, Movie.class);
                    }
                }
            }

            throw new MovieNotFoundException("Película no encontrada con ID: " + id);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error de conexión", e);
            throw new MovieServiceException("Error de conexión con el servidor");
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Petición interrumpida", e);
            Thread.currentThread().interrupt();
            throw new MovieServiceException("La petición fue interrumpida");
        } catch (JsonParseException e) {
            logger.log(Level.SEVERE, "Error al procesar JSON", e);
            throw new MovieServiceException("Error al procesar la respuesta del servidor");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado", e);
            throw new MovieServiceException("Error inesperado: " + e.getMessage());
        }
    }

    private void validateId(String id) throws MovieServiceException {
        try {
            int movieId = Integer.parseInt(id);
            if (movieId < 1 || movieId > 7) {
                throw new MovieServiceException("El ID debe estar entre 1 y 7");
            }
        } catch (NumberFormatException e) {
            throw new MovieServiceException("ID inválido: debe ser un número");
        }
    }
}