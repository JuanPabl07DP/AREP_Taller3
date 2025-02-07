package co.edu.escuelaing.arem.ASE;

import co.edu.escuelaing.arem.ASE.annotations.RestController;
import co.edu.escuelaing.arem.ASE.controller.MovieController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RestController
public class MovieControllerTest {

    private MovieController controller;

    @BeforeEach
    void setUp() {
        controller = new MovieController();
    }

    @Test
    void shouldValidateMovieId() {
        String result = controller.getMovie("8");
        assertTrue(result.contains("error"), "Debería retornar un error para ID inválido");

        result = controller.getMovie("0");
        assertTrue(result.contains("error"), "Debería retornar un error para ID inválido");

        result = controller.getMovie("abc");
        assertTrue(result.contains("error"), "Debería retornar un error para ID no numérico");
    }

    @Test
    void shouldReturnValidMovie() {
        String result = controller.getMovie("4");
        assertTrue(result.contains("A New Hope"), "Debería retornar la película correcta");
        assertFalse(result.contains("error"), "No debería contener errores");
    }
}
