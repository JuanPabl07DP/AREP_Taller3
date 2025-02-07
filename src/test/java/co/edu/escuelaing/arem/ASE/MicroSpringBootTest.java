package co.edu.escuelaing.arem.ASE;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import co.edu.escuelaing.arem.ASE.annotations.GetMapping;
import co.edu.escuelaing.arem.ASE.annotations.RequestParam;
import co.edu.escuelaing.arem.ASE.annotations.RestController;
import java.lang.reflect.Method;

public class MicroSpringBootTest {

    @RestController
    static class TestController {
        @GetMapping("/test")
        public String test() {
            return "test";
        }

        @GetMapping("/hello")
        public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
            return "Hello, " + name + "!";
        }
    }

    private TestController controller;

    @BeforeEach
    void setUp() {
        controller = new TestController();
    }

    @Test
    void shouldDetectRestControllerAnnotation() {
        assertTrue(TestController.class.isAnnotationPresent(RestController.class),
                "La clase debería tener la anotación @RestController");
    }

    @Test
    void shouldDetectGetMappingAnnotation() {
        Method[] methods = TestController.class.getDeclaredMethods();
        boolean hasGetMapping = false;
        for (Method method : methods) {
            if (method.isAnnotationPresent(GetMapping.class)) {
                hasGetMapping = true;
                break;
            }
        }
        assertTrue(hasGetMapping, "Debería haber al menos un método con @GetMapping");
    }

    @Test
    void shouldHaveCorrectGetMappingPath() {
        Method[] methods = TestController.class.getDeclaredMethods();
        String path = "";
        for (Method method : methods) {
            if (method.getName().equals("test")) {
                GetMapping mapping = method.getAnnotation(GetMapping.class);
                path = mapping.value();
                break;
            }
        }
        assertEquals("/test", path, "El path del mapping debería ser /test");
    }

    @Test
    void shouldProcessRequestParam() {
        Method[] methods = TestController.class.getDeclaredMethods();
        String defaultValue = "";
        String paramName = "";
        for (Method method : methods) {
            if (method.getName().equals("hello")) {
                var params = method.getParameters();
                for (var param : params) {
                    RequestParam rp = param.getAnnotation(RequestParam.class);
                    if (rp != null) {
                        defaultValue = rp.defaultValue();
                        paramName = rp.value();
                    }
                }
                break;
            }
        }
        assertEquals("World", defaultValue, "El valor por defecto debería ser 'World'");
        assertEquals("name", paramName, "El nombre del parámetro debería ser 'name'");
    }

    @Test
    void shouldInvokeMethodCorrectly() throws Exception {
        Method helloMethod = TestController.class.getMethod("hello", String.class);
        String result = (String) helloMethod.invoke(controller, "Test");
        assertEquals("Hello, Test!", result, "El método debería retornar el saludo correcto");
    }
}

