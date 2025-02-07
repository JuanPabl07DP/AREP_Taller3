package co.edu.escuelaing.arem.ASE.controller;

import co.edu.escuelaing.arem.ASE.annotations.RestController;
import co.edu.escuelaing.arem.ASE.annotations.GetMapping;
import co.edu.escuelaing.arem.ASE.annotations.RequestParam;
import co.edu.escuelaing.arem.ASE.model.Movie;
import co.edu.escuelaing.arem.ASE.service.MovieService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class MovieController {
    private final MovieService movieService;
    private final Gson gson;
    private static final Logger logger = Logger.getLogger(MovieController.class.getName());

    public MovieController() {
        this.movieService = new MovieService();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()  // Para mejor formato del JSON
                .create();
    }

    @GetMapping("/api/film")
    public String getMovie(@RequestParam(value = "id", defaultValue = "1") String id) {
        logger.info("Recibida solicitud para película con ID: " + id);

        try {
            // Validación del ID
            if (id == null || id.trim().isEmpty()) {
                return gson.toJson(new ErrorResponse("El ID de la película no puede estar vacío"));
            }

            int movieId;
            try {
                movieId = Integer.parseInt(id);
                if (movieId < 1 || movieId > 7) {
                    return gson.toJson(new ErrorResponse("El ID de la película debe estar entre 1 y 7"));
                }
            } catch (NumberFormatException e) {
                return gson.toJson(new ErrorResponse("El ID de la película debe ser un número válido"));
            }

            Movie movie = movieService.getMovieById(id);

            JsonObject response = new JsonObject();
            response.addProperty("title", movie.getTitle());
            response.addProperty("episode_id", movie.getEpisodeId());
            response.addProperty("opening_crawl", movie.getOpeningCrawl());
            response.addProperty("director", movie.getDirector());
            response.addProperty("producer", movie.getProducer());
            response.addProperty("release_date", movie.getReleaseDate());

            String jsonResponse = gson.toJson(response);
            logger.info("Respuesta generada: " + jsonResponse);
            return jsonResponse;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al procesar la solicitud", e);
            return gson.toJson(new ErrorResponse(e.getMessage()));
        }
    }

    private static class ErrorResponse {
        private final String error;

        public ErrorResponse(String message) {
            this.error = message;
        }
    }
}
