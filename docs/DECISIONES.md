# Decisiones y supuestos: prueba de automatización de API

Algunos puntos del enunciado admiten más de una interpretación. Este documento registra cada decisión, su motivo y, cuando aplica, la evidencia obtenida contra la API real (verificada el 23-09-2026).

## Criterios

Cada ambigüedad se resolvió con este orden de prioridad:

1. Lo explícito y concreto prevalece sobre lo genérico: un comando concreto pesa más que "X o Y".
2. Ante una contradicción, prevalece lo que el documento repite o desarrolla más.
3. El comportamiento real de la API se verifica antes de diseñar la prueba.
4. El resultado esperado sale del enunciado y de la documentación oficial (apidoc). Donde no dicen nada, de estándares: RFC 9110 (HTTP) y OWASP.

**Principio de reporte:** las pruebas reportan lo que la API hace. Si la API no cumple lo esperado, la prueba falla y el defecto queda registrado. No se relajan aserciones, no se reintentan fallos y no se deshabilitan pruebas para conseguir una ejecución en verde.

## Stack

| Tema | Decisión | Motivo |
|---|---|---|
| Lenguaje | Java 21 (`maven.compiler.release=21`) | Requisito explícito. Compila con JDK 21 o superior. |
| Build | Maven | Lo nombran el encabezado y todos los comandos del README (`mvn clean test`, `mvn allure:serve`). |
| Core | Rest Assured 6.0.1 | Cumple "Rest Assured 5.x+" con la versión vigente. |
| Runner | JUnit 5.14 (Jupiter) | Paralelismo por configuración, pruebas parametrizadas e integración directa con Allure. |
| Reporte | Allure 2.35 | El README exige `mvn allure:serve`. Se usa la línea 2.x, madura; Allure Java 3.0 salió días antes de esta entrega. El filtro `AllureRestAssured` adjunta cada petición y respuesta. |
| Datos de prueba | Datafaker | Sucesor mantenido de JavaFaker, que no se actualiza desde 2020 y arrastra dependencias con vulnerabilidades conocidas. |
| Serialización | Jackson 2 + `jackson-datatype-jsr310` | Requisito del enunciado; las fechas se mapean como `LocalDate`. |
| Aserciones | Hamcrest sobre la respuesta; AssertJ sobre objetos | AssertJ compara objetos completos y sus *soft assertions* reportan todos los fallos de una vez. |

## Decisiones

### D-01. Patrón de diseño: Screenplay o API Object

- **Ambigüedad:** el encabezado define el enfoque como Screenplay; la sección 1 pide API Object Pattern con Builder.
- **Decisión:** Screenplay como arquitectura de las pruebas, con API Objects debajo:
  `Prueba → Screenplay (Actor, Ability, Task, Question, Consequence) → API Objects (un cliente por recurso) → Rest Assured`.
  Los payloads se construyen con `@Builder` de Lombok.
- **Motivo:** los dos patrones no se excluyen. Screenplay expresa la intención ("el actor crea una reserva") y los API Objects encapsulan el contrato HTTP. Así se cumple la separación entre contratos, datos y aserciones que pide la sección 1.

### D-02. Screenplay propio, sin Serenity BDD

- **Ambigüedad:** Screenplay suele implementarse con Serenity BDD, que no aparece en el stack requerido.
- **Decisión:** implementación propia y ligera de Screenplay.
- **Motivo:** Serenity trae su propio runner y su propio reporte, que duplicarían el Allure exigido. La implementación propia usa los mismos conceptos y nombres (`whoCan`, `attemptsTo`, `asksFor`, `should`), así que migrar a Serenity sería directo.

### D-03. Escenario 3: el listado de reservas no incluye `totalprice`

- **Evidencia:** `GET /booking` devuelve solo `[{"bookingid": n}]`. El listado es compartido por todos los usuarios de la API pública y cambia constantemente: pasó de unos 900 a unos 2.100 registros en menos de media hora.
- **Decisión:** dos pruebas.
  1. `GET /booking` cumple su contrato: cada elemento contiene solo `bookingid` (schema `booking-ids-schema.json`).
  2. Filtro por precio:
     - Se crean reservas propias con un `firstname` único.
     - `GET /booking?firstname=<único>` devuelve sus IDs (filtrado por query params, como describe la tabla de endpoints).
     - Cada ID se completa con su detalle en una colección `[{bookingid, ...detalle}]` (pregunta `TheBookingDetails`).
     - Una sola expresión GPath filtra y extrae: `findAll { it.totalprice > umbral }.bookingid`.
     - Uno de los IDs resultantes se consulta y se mapea a `Booking.class`.
- **Motivo:** cumple la intención del escenario (filtrar con GPath sin bucles y mapear a una clase tipada) sin depender de datos de terceros. Recorrer el listado completo supondría miles de peticiones a un servicio compartido.

### D-04. Umbral de precio no definido

- **Decisión:** umbral configurable (`booker.price-threshold`, 500 por defecto) con análisis de valores límite. Se crean reservas con precio umbral − 1, umbral y umbral + 1. Resultado esperado: solo la de umbral + 1, porque "mayor a" es estricto.

### D-05. "Sin bucles `for`"

- **Decisión:** el filtrado y la extracción se hacen solo con GPath. Los Streams se usan únicamente para orquestar las llamadas HTTP que completan el detalle. No hay bucles imperativos en el proyecto.

### D-06. Headers "de seguridad o servidor" (escenario 4)

- **Evidencia:** en `qa` la respuesta trae `Content-Type: application/json; charset=utf-8`, `Date`, `Server: Heroku`, `Via` y `X-Powered-By: Express`. En `local` (sin Heroku) no aparecen `Server` ni `Via`: los agrega el router de Heroku, no la aplicación. En ningún ambiente trae `Strict-Transport-Security`, `X-Content-Type-Options` ni `Cache-Control`.
- **Decisión:** tres pruebas separadas, para que un fallo no oculte los demás:
  1. `Content-Type` exacto (el ejemplo del enunciado): pasa.
  2. Header de servidor `Date`, obligatorio para todo servidor de origen según RFC 9110 y con formato de fecha HTTP: pasa en los dos ambientes.
  3. Headers que OWASP recomienda para APIs REST (`Strict-Transport-Security`, `X-Content-Type-Options: nosniff`, `Cache-Control: no-store`) y ausencia de `X-Powered-By`: **falla** y se reporta como DEF-06. Con *soft assertions*, una sola ejecución lista todo lo que falta.
- **Corrección:** la segunda prueba validaba antes la presencia de `Server`. Pasaba en `qa` y fallaba en `local`, porque en realidad validaba la infraestructura de Heroku y no el producto; además, OWASP recomienda no exponer ese header. Se cambió el oráculo, no se relajó una verificación del producto.

### D-07. JSON Schema no provisto; validación "estricta"

- **Decisión:** schema propio en `src/test/resources/schemas/booking-schema.json`, derivado de la apidoc: los campos que la apidoc marca como obligatorios van en `required`, los tipos son los documentados, `additionalProperties: false` en todos los niveles y las fechas usan el patrón `YYYY-MM-DD`.
- **Nota:** la apidoc marca `additionalneeds` como obligatorio, pero la API acepta reservas sin ese campo y responde sin él (DEF-07). El schema sigue el contrato documentado.

### D-08. SLA de 2.000 ms

- **Evidencia:** tras un rato sin uso, la primera petición tardó 4,4 s (arranque en frío del servicio en Heroku). En caliente, las respuestas rondan 0,4 s.
- **Decisión:**
  - Antes de medir se hace un `GET /ping` de calentamiento. Su tiempo queda como parámetro en el reporte; no se descarta.
  - La petición medida usa `time(lessThan(2000L), MILLISECONDS)`.
  - Si supera el límite, la prueba falla. Sin reintentos.
- **Limitación:** es una sola muestra, medida desde el cliente (incluye la red de quien ejecuta). No sustituye una prueba de rendimiento con percentiles (JMeter o k6).

### D-09. "Reserva existente"

- **Decisión:** cada prueba crea su propia reserva y la elimina al terminar (`CleanUpCreatedBookings` en `@AfterEach`, con un token propio).
- **Motivo:** independencia entre pruebas y ejecución en paralelo sobre un ambiente cuyos datos modifican terceros. Si la API acepta por error una reserva inválida, también se registra y se elimina.

### D-10. Token caducado (escenario 5)

- **Decisión:** se automatizan dos casos: sin token y con un token falso con el formato de los reales (15 caracteres hexadecimales). Ambos esperan 403, pasan y verifican además que la reserva no cambió. El token caducado no se automatiza: la API no documenta cuánto dura un token ni permite invalidarlo. Queda registrado como brecha de cobertura.

### D-11. Qué cuenta como "5 casos de prueba"

- **Decisión:** 5 escenarios = 5 clases de prueba, cada una con los métodos que necesite. El escenario 5 ("manejo negativo y edge cases") incluye, además de los casos pedidos, los que detectan DEF-01 a DEF-05 y DEF-07. El README incluye una matriz de trazabilidad escenario → clase.

### D-12. DELETE sin escenario propio

- **Decisión:** el escenario 1 recorre el ciclo completo con el mismo token: crear → PUT (200) → DELETE (201) → GET (404). Funciona además como prueba de transición de estados.

### D-13. Reutilizar la sesión sin romper el paralelismo

- **Decisión:** cada actor obtiene su token una vez y lo reutiliza en todas sus peticiones de modificación. Un `Filter` de Rest Assured (`AuthTokenFilter`) inyecta `Cookie: token=<valor>` en PUT, PATCH y DELETE. Cada prueba tiene su propio actor. No se usa configuración estática de Rest Assured (`RestAssured.baseURI` y similares), que no es segura entre hilos.
- **Motivo:** se reutiliza la sesión sin compartir estado mutable entre hilos.

### D-14. Ambientes (`-Denv`)

- **Decisión:** un archivo de propiedades por ambiente:
  - `qa` (por defecto): la API pública.
  - `local`: el contenedor Docker oficial (`mwinteringham/restfulbooker`).
- **Motivo:** la API pública es el único ambiente disponible. El contenedor aporta un segundo ambiente real, aislado y sin datos de terceros, que usa el pipeline de CI.

### D-15. Credenciales y token

- **Decisión:** cada valor se resuelve en este orden: propiedad de sistema, variable de entorno (`BOOKER_USERNAME`, `BOOKER_PASSWORD`, `BOOKER_BASE_URI`...) y archivo del ambiente. Los archivos solo contienen las credenciales de demostración que publica la propia API. El token nunca se guarda: se obtiene en cada ejecución. Un ambiente con credenciales reales las recibiría solo por variables de entorno (en CI, GitHub Secrets).
- **Motivo:** quien evalúa puede ejecutar sin configurar nada, y el mismo mecanismo sirve para ambientes con credenciales reales.

### D-16. Códigos de estado que se desvían del estándar

- **Contexto:** la API responde 200 al crear (no 201), 201 al eliminar (no 200 ni 204) y 403 cuando falta la autenticación (no 401).
- **Decisión:** las pruebas validan lo que definen el enunciado y la apidoc, así que pasan. La desviación respecto a RFC 9110 se registra como observación y no como prueba fallida: la base de prueba define esos códigos explícitamente.

### D-17. Historial de commits

- **Decisión:** Conventional Commits (`feat:`, `test:`, `docs:`, `ci:`), atómicos y en orden lógico: configuración → modelos → Screenplay → escenarios → CI → documentación. Sin aplastar todo en un solo commit, para que se vea el proceso.

## Notas técnicas descubiertas durante la implementación

- **`Accept` exacto en la especificación base.** Con `setAccept(ContentType.JSON)`, Rest Assured envía una lista de cuatro tipos y la API responde 418 (DEF-05). La especificación base envía exactamente `application/json` para que los demás escenarios no dependan de ese defecto.
- **`log().ifValidationFails()` se configura en `LogConfig`.** Se comprobó que `then().spec(responseSpec)` ignora el log declarado en `given()`. `enableLoggingOfRequestAndResponseIfValidationFails` imprime petición y respuesta en todos los casos, incluidas las validaciones con `ResponseSpecBuilder`.
- **Una petición sin `Accept` no se puede armar con filtros.** Rest Assured vuelve a añadir `Accept: */*` antes de cada filtro y al enviar. El header se elimina en el cliente HTTP, y como el registro de Rest Assured en Allure sigue mostrando `*/*`, los headers realmente enviados se adjuntan aparte.
- **Comparar ambientes separa producto de infraestructura.** La primera ejecución en CI dio 11 fallas en `qa` y 12 en `local`. Las 11 comunes son los 7 defectos, así que son del código de la aplicación y no de Heroku. La falla que solo ocurría en `local` era la prueba del header `Server`, que dependía del router de Heroku (ver D-06).

## Registro de defectos

Verificados contra la API pública el 23-09-2026 y reproducidos en CI en los dos ambientes (`qa` y `local`) el 24-09-2026: son defectos del código de la aplicación, no de la infraestructura. Cada defecto tiene al menos una prueba automatizada que falla mientras siga presente. La propia página de Restful-Booker aclara que la API incluye defectos intencionales para explorar.

| ID | Hallazgo | Esperado | Obtenido | Severidad | Escenario |
|---|---|---|---|---|---|
| DEF-01 | Crear una reserva sin los campos obligatorios | 400 Bad Request | 500 Internal Server Error | Alta | 5 |
| DEF-02 | Se aceptan checkout anterior al checkin y precio negativo | 400 Bad Request | 200, reserva creada | Media | 5 |
| DEF-03 | Credenciales inválidas en `/auth` | 401 Unauthorized | 200 con `{"reason":"Bad credentials"}` | Media | 5 |
| DEF-04 | PUT o DELETE sobre una reserva inexistente con token válido | 404 Not Found | 405 Method Not Allowed | Baja | 5 |
| DEF-05 | Negociación de contenido: sin header `Accept`, con una lista de tipos que incluye JSON (el valor por defecto de axios) o con un tipo no disponible | 200 con la representación por defecto; 406 o representación por defecto para tipos no disponibles (RFC 9110) | 418 I'm a teapot en los tres casos | Media | 5 |
| DEF-06 | Faltan headers de seguridad y se expone la tecnología del servidor | HSTS, `nosniff`, `Cache-Control`; sin `X-Powered-By` | Ausentes; `X-Powered-By: Express` | Media | 4 |
| DEF-07 | `additionalneeds` es obligatorio según la apidoc | 400 si falta; campo siempre presente en la respuesta | Se acepta sin el campo y la respuesta lo omite | Baja | 5 |

Resultado de la ejecución de referencia, en cada ambiente: 23 pruebas, 12 pasan y 11 fallan. Las 11 fallas corresponden a estos 7 defectos.

## Observaciones (no se automatizan como fallo)

- Códigos de estado documentados que se desvían de RFC 9110 (D-16).
- La API no permite filtrar por precio ni consultar detalles en lote: filtrar por precio exige N + 1 llamadas (D-03).
- El ambiente público tiene arranque en frío (4,4 s) y durante la verificación devolvió respuestas vacías de forma intermitente. Se trata como riesgo de ambiente, no como defecto del producto.

## Brechas de cobertura y riesgos

- Token caducado: no se puede provocar desde la prueba (D-10).
- SLA: una sola muestra medida desde el cliente (D-08).
- Ambiente compartido: mitigado con datos propios en cada prueba y con el ambiente `local` (D-09, D-14).

## Clasificación de resultados en el reporte

`src/test/allure/categories.json` separa los resultados en:

- **Defectos conocidos del producto:** aserción fallida cuyo mensaje incluye el ID del defecto (`[DEF-xx]`).
- **Defectos nuevos del producto:** aserción fallida sin defecto registrado; requiere análisis y un nuevo reporte.
- **Problemas de ambiente:** timeouts, conexión rechazada o respuestas 502/503/504.
- **Errores de automatización:** excepción inesperada en el código de prueba (estado *broken*).

Mientras estos defectos existan, una ejecución en rojo es el resultado correcto.

## Integración continua

Pipeline de GitHub Actions (`.github/workflows/api-tests.yml`):

- Se ejecuta en cada push a `main`, en cada pull request y a demanda.
- Corre la suite contra `local` (contenedor como servicio del job) y contra `qa`, en paralelo.
- Publica el reporte de Allure siempre (`if: always()`), también cuando hay fallos.
- El estado del job refleja el resultado real de la suite.
