package co.edu.escuelaing.arem.ASE.service;

public class MovieNotFoundException extends MovieServiceException {
    public MovieNotFoundException(String message) {
        super(message);
    }
}
