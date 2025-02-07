package co.edu.escuelaing.arem.ASE;

import co.edu.escuelaing.arem.ASE.model.Movie;
import co.edu.escuelaing.arem.ASE.service.MovieService;
import co.edu.escuelaing.arem.ASE.service.MovieServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Tests para el MovieService
public class MovieServiceTest {

    private MovieService service;

    @BeforeEach
    void setUp() {
        service = new MovieService();
    }

    @Test
    void shouldHandleValidRequest() {
        assertDoesNotThrow(() -> {
            Movie movie = service.getMovieById("4");
            assertNotNull(movie);
            assertEquals("A New Hope", movie.getTitle());
        });
    }

    @Test
    void shouldHandleInvalidId() {
        assertThrows(MovieServiceException.class, () -> {
            service.getMovieById("999");
        });
    }

    @Test
    void shouldHandleNonNumericId() {
        assertThrows(MovieServiceException.class, () -> {
            service.getMovieById("abc");
        });
    }
}
