# **Star Wars Movie API - Taller 3 AREP**
![](img/img.png)
![](img/img_1.png)
## 📌 Descripción

Este proyecto implementa un servidor web personalizado en Java con capacidades similares a Apache y Spring Boot. El servidor puede entregar páginas HTML e imágenes PNG, y proporciona un framework IoC (Inversión de Control) para construir aplicaciones web a partir de POJOs. Como ejemplo, se ha implementado una aplicación que consulta información sobre las películas de Star Wars utilizando la API SWAPI.

## 🚀 Tecnologías Utilizadas

- Java 8+
- Framework IoC personalizado (similar a Spring Boot)
- Maven
- Gson
- HTML, CSS y JavaScript
- SWAPI (Star Wars API)
- Anotaciones personalizadas (@RestController, @GetMapping, @RequestParam)

## 🔍 Características Principales

- Servidor HTTP personalizado
- Framework IoC con capacidades reflexivas
- Soporte para anotaciones tipo Spring
- Carga de componentes por línea de comandos o escaneo automático
- Manejo de archivos estáticos (HTML, PNG, etc.)
- Procesamiento de parámetros de solicitud
- Sistema de logging integrado

## 🎬 Funcionamiento de la Aplicación

1. El usuario ingresa el ID del episodio de Star Wars (1-7) en la interfaz web.

![](img/img_2.png)

2. La aplicación realiza una solicitud al endpoint `/api/film` que internamente consulta la API de SWAPI.

3. La información es procesada y mostrada en la interfaz de forma estructurada.

4. Se mantiene un historial de las películas consultadas durante la sesión.

![](img/img_3.png)

## 🔧 Instalación y Ejecución

1️⃣ **Clonar el repositorio**

```
git clone https://github.com/JuanPabl07DP/AREP_Taller3.git
cd AREP_Taller3
```

2️⃣ **Compilar el proyecto con Maven**

```
mvn clean install
```

3️⃣ **Ejecutar el servidor**

```
# Método 1: Especificando el controlador
java -cp target/classes co.edu.escuelaing.arem.ASE.MicroSpringBoot co.edu.escuelaing.arem.ASE.controller.MovieController

# Método 2: Escaneo automático
java -cp target/classes co.edu.escuelaing.arem.ASE.MicroSpringBoot
```

4️⃣ **Acceder a la aplicación**

Abre tu navegador y dirígete a:

```
http://localhost:8080/
```

## 🌟 Ejecución de pruebas

El proyecto incluye pruebas unitarias que verifican:

- Detección correcta de anotaciones (@RestController, @GetMapping, @RequestParam)
- Mapeo de rutas y procesamiento de parámetros
- Validación de IDs de películas
- Manejo de errores y casos límite
- Integración con el servicio de películas

Para ejecutar las pruebas:
```
mvn test
```

## 🔨 Arquitectura

El proyecto sigue una arquitectura modular:

1. Framework IoC (MicroSpringBoot):

- Escaneo de componentes
- Manejo de anotaciones
- Servidor HTTP integrado
- Enrutamiento de solicitudes

2. Controladores:

- MovieController: Maneja las peticiones relacionadas con películas
- Procesamiento de parámetros
- Validación de entrada
- Manejo de errores

3. Servicios:

- MovieService: Interactúa con SWAPI
- Mapeo de respuestas
- Cache de resultados

4. Cliente Web:

- Interfaz de usuario interactiva
- Manejo de estado local
- Visualización de resultados
- Historial de consultas

## 🔗 Endpoints Disponibles

-  ```GET /movie/{id}``` → Devuelve información sobre la película correspondiente al episodio indicado.

- ```GET /``` → Página principal de la aplicación.

- Archivos estáticos en ```/public/```

## ⚠️ Manejo de errores

El sistema incluye manejo robusto de errores para:

- IDs de película inválidos
- Errores de conexión con SWAPI
- Errores de parsing JSON
- Recursos no encontrados
- Métodos HTTP no soportados

### 📌 Autores:
- Juan Pablo Daza Pereira (JuanPabl07DP)

### 📅 Fecha de creación: Febrero 2025
