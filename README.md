# reserva — Microservicio de gestión de reservas de coworking

Prueba técnica para Desarrollador Backend Java Spring Boot (Banco Cuscatlán / Tribu Digital).
Microservicio para gestionar reservas de espacios de coworking: usuarios, espacios y reservas,
con control de solapamiento de fechas, validación de pago externa resiliente y notificación
asíncrona al confirmar una reserva.

## Estado actual

Este README se mantiene actualizado a medida que avanza la implementación. Ahora mismo el
proyecto tiene resuelta la infraestructura base (build, dependencias, entorno de datos) y está
en desarrollo el dominio y las reglas de negocio. La sección [Progreso](#progreso) detalla qué
está hecho y qué falta.

## Stack técnico

- Java 21 (LTS)
- Spring Boot 4.x
- Spring Data JPA (PostgreSQL)
- `Dockerfile` de la app + `docker-compose.yml` (Postgres + app)
- Spring Security + JWT (`jjwt`)
- Bean Validation (jakarta.validation)
- Spring Boot Actuator
- Spring Cache
- Resilience4j (Circuit Breaker) vía `spring-cloud-starter-circuitbreaker-resilience4j`
- MapStruct + Lombok
- springdoc-openapi (Swagger UI)
- PostgreSQL 16 (Docker Compose)
- JUnit + Mockito, Testcontainers, WireMock (testing)

### Sobre la versión de Spring Boot

El enunciado pide explícitamente "Spring Boot 3.x + Java 17+". Este proyecto usa **Spring Boot
4.0.7** en vez de 3.x — Java 21 sí cumple el "17+" (es un piso, no una versión exacta), pero
4.0.7 es una desviación consciente de lo pedido, y vale la pena explicar por qué:

- El proyecto arrancó ya sobre Boot 4.0.7 (era la versión que trajo el `spring-boot-starter-parent`
  al generar el setup inicial) y, al día 1, el costo de bajar a 3.x era bajo. Se decidió no
  bajarlo y quedarse en 4.0.7 para trabajar con la versión más reciente del framework en vez de
  retroceder a la pedida por el enunciado — con la salvedad de que **no es lo que pide el
  enunciado literalmente**, y se documenta aquí para que quede explícito, no oculto.
- Es la misma línea LTS de Java (21) y el mismo modelo mental de Spring Boot; el riesgo real no
  es de incompatibilidad, sino de que Boot 4 **modulariza la autoconfiguración** por feature: por
  ejemplo, `flyway-core` solo (sin `spring-boot-starter-flyway`) no auto-configura Flyway en Boot
  4, algo que sí pasaba en 3.x. Cualquier receta pensada para 3.x puede fallar en silencio hasta
  que se agrega el starter correcto — ya pasó una vez con Flyway durante el desarrollo (ver
  historial de commits).
- Trade-off asumido: se prioriza demostrar dominio de la versión vigente del framework sobre el
  cumplimiento literal del número de versión pedido. Si esto pesa en la evaluación, el cambio a
  3.x en este punto del proyecto (solo entidades, Flyway y Dockerfile escritos) seguiría siendo
  relativamente barato.

## Arquitectura

Capas estrictas, sin saltarse niveles:

```
controller  -> recibe HTTP, valida DTO de entrada, sin lógica de negocio
service     -> lógica de negocio, transacciones (@Transactional), reglas de dominio
repository  -> Spring Data JPA, queries con @Query/Specifications donde aplique
dto         -> nunca se exponen entidades JPA directamente en los controllers
mapper      -> MapStruct entre entidad y DTO
exception   -> excepciones de negocio propias (ej. OverlappingReservationException)
```

### Dominio principal

- **User** (id, email, password, role: `ADMIN` / `USER`). Registro público vía `POST /auth/register`
  (o similar) crea siempre `USER` — no hay forma de auto-registrarse como `ADMIN`.
- **Space** (id, nombre, tipo, capacidad, ubicación, tarifa/hora). El enunciado no especifica
  quién gestiona el CRUD de espacios; se asume que crear/editar/eliminar es solo `ADMIN`, y que
  la consulta (listar/ver) está abierta a ambos roles, ya que un `USER` necesita ver los espacios
  disponibles para poder reservarlos.
- **Reservation** (id, user, space, fechaInicio, fechaFin, estado)
  - Estados: `PENDING` → `CONFIRMED` → `COMPLETED`, o `CANCELLED` (terminal)
  - Estado adicional de resiliencia: `PENDING_PAYMENT` (fallback del circuit breaker)

### Patrón GoF: State

El ciclo de vida de `Reservation` se modela con el patrón **State**: cada estado (`Pending`,
`Confirmed`, `Completed`, `Cancelled`, `PendingPayment`) es una implementación que sabe cuáles
transiciones son válidas desde sí misma. Se eligió State en vez de un `if/else`/`switch`
disperso en el service porque:

- Las reglas de transición quedan encapsuladas junto al estado al que pertenecen, en vez de
  esparcidas por todo el `ReservationService`.
- Agregar un estado nuevo (o cambiar sus transiciones válidas) no obliga a tocar un método
  gigante de decisión centralizado — se agrega/edita una clase de estado.
- Evita que el service acumule lógica condicional que crece con cada estado nuevo, algo muy
  propenso a bugs en máquinas de estado con más de 3-4 estados como esta.

Se descartó Strategy porque el problema no es "elegir un algoritmo intercambiable" (p. ej. una
forma de calcular tarifa) sino "qué transiciones son válidas según en qué estado estoy", que es
exactamente el caso de uso que State resuelve.

### Reglas de negocio clave

- No se permiten reservas solapadas para el mismo espacio (validación transaccional).
- `USER` solo puede crear/consultar/cancelar sus propias reservas; `ADMIN` ve y gestiona todas.
- Al confirmar una reserva:
  1. Se invoca el servicio externo simulado de validación de pago, protegido con Resilience4j.
     Si el circuito está abierto, el fallback deja la reserva en `PENDING_PAYMENT` en vez de
     fallar la petición.
  2. Se dispara una notificación asíncrona (simula el envío de un correo, vía log/mock) que no
     bloquea la respuesta HTTP.
- Endpoint de reporte: ocupación (%) de cada espacio en un rango de fechas, cacheado con
  `@Cacheable` e invalidado con `@CacheEvict` cuando una reserva cambia de estado dentro de ese
  rango.

## Cómo ejecutar el proyecto

### Requisitos

- JDK 21
- Docker Desktop (para PostgreSQL vía Docker Compose)

### Opción A — todo en Docker (app + Postgres)

```bash
docker compose up --build
```

Levanta `reserva-postgres` (puerto **5433** del host, para no chocar con un Postgres nativo) y
`reserva-app` (puerto **8080**), con la app corriendo en el perfil `docker` — conecta a Postgres
por el hostname interno del compose (`postgres:5432`), no por `localhost`. Al arrancar, Flyway
aplica las migraciones de `src/main/resources/db/migration/` contra ese Postgres antes de que
Hibernate valide el esquema.

### Opción B — Postgres en Docker, app en local

```bash
docker compose up -d postgres
./mvnw clean compile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

En Windows PowerShell, si `JAVA_HOME` de la sesión no apunta todavía a JDK 21:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
.\mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
```

La app queda escuchando en `http://localhost:8080` conectando a Postgres en `localhost:5433`.

### 3. Verificar que levantó bien

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health check: http://localhost:8080/actuator/health
- Estado del circuit breaker: http://localhost:8080/actuator/circuitbreakers

### Perfiles

- `dev` (`application-dev.yaml`): apunta al Postgres de Docker Compose vía el puerto publicado en
  el host (`localhost:5433`), `ddl-auto: validate` y SQL logging activado para desarrollo local.
- `docker` (`application-docker.yaml`): igual que `dev`, pero apunta al Postgres por el hostname
  interno del compose (`postgres:5432`) — es el perfil que usa el servicio `app` del
  `docker-compose.yml`.
- `prod` (`application-prod.yaml`): pendiente de definir.

### Esquema de base de datos y migraciones

El esquema (`users`, `spaces`, `reservations`) se versiona con **Flyway**
(`src/main/resources/db/migration/V1__init.sql`), no con `ddl-auto`. Razón: la regla de no
solapamiento de reservas se implementa como un `EXCLUDE` constraint de PostgreSQL sobre rango de
tiempo (extensión `btree_gist`), algo que Hibernate no puede generar a partir de anotaciones JPA.
`ddl-auto` queda en `validate` en todos los perfiles — solo confirma que las entidades coincidan
con lo que Flyway ya creó.

### Autenticación

`POST /api/auth/register` (crea siempre un `USER`, nunca `ADMIN` vía signup público) y
`POST /api/auth/login` devuelven un JWT firmado con HS512 (`jjwt`). El resto de endpoints exige
`Authorization: Bearer <token>`; sin token responde `401` y con token pero rol insuficiente
responde `403` (`RestAuthenticationEntryPoint` / `RestAccessDeniedHandler`, con cuerpo JSON
consistente con el resto de errores de la API vía `ApiError`) — Spring Security intercepta esas
peticiones antes de llegar al `DispatcherServlet`, así que el `@RestControllerAdvice` normal no
alcanza a esos dos casos y hace falta manejarlos aparte en la configuración de seguridad.

El secreto de firma (`app.jwt.secret`) tiene un valor por defecto en `application.yaml` **solo
para desarrollo**; se sobreescribe con la variable de entorno `JWT_SECRET` en cualquier entorno
real — nunca se versiona un secreto de producción.

#### Usuario ADMIN por defecto (solo dev)

Como el registro público (`POST /api/auth/register`) siempre crea un `USER`, hace falta otra vía
para tener un primer `ADMIN` con el que probar los endpoints restringidos a ese rol (CRUD de
`Space`, por ejemplo). `AdminSeedRunner` crea uno automáticamente al arrancar la app, si no existe
ya (idempotente, por email):

- Email: `admin@reserva.local`
- Password: `Admin123!`

Configurable vía `ADMIN_SEED_EMAIL` / `ADMIN_SEED_PASSWORD` / `ADMIN_SEED_ENABLED`. En un entorno
real se desactivaría (`ADMIN_SEED_ENABLED=false`) y el alta de administradores se haría por otra
vía administrativa, no por seed automático.

### Circuit breaker y confirmación de pago

`POST /api/reservations/{id}/confirm` invoca `PaymentValidationService.validate(reservation)`,
anotado con `@CircuitBreaker(name = "paymentValidation")` (resilience4j vía anotación — ver nota
de dependencia más abajo). Esa validación le pega, vía `RestClient`, a
`MockPaymentGatewayController` (`/mock/payment-gateway/validate`), que simula el proveedor de pago
externo del enunciado: configurable con `MOCK_PAYMENT_FAILURE_RATE` (probabilidad de que el mock
devuelva `503`, no un rechazo de negocio — el circuit breaker debe reaccionar a que el proveedor
esté caído/inestable, no a que un pago puntual sea rechazado) y `MOCK_PAYMENT_DELAY_MILLIS`.

- Si la validación aprueba → la reserva pasa a `CONFIRMED` (patrón State: `confirm()`).
- Si falla (error real o circuito abierto, vía `fallbackMethod`) → la reserva pasa a
  `PENDING_PAYMENT` (`holdForPayment()`) y la petición **igual responde `200`**, nunca falla —
  regla de negocio explícita del enunciado.
- Umbrales configurados en `application.yaml` (`resilience4j.circuitbreaker.instances.*`), no
  hardcodeados: ventana de 10 llamadas, mínimo 5 para evaluar, 50% de fallas abre el circuito.
- Verificado manualmente subiendo `MOCK_PAYMENT_FAILURE_RATE=1` en `docker-compose.yml` y
  confirmando la misma reserva varias veces seguidas: a la 5ª falla el circuito pasa a `OPEN`
  (visible en `/actuator/circuitbreakers` y `/actuator/health`) y el siguiente intento se rechaza
  sin siquiera llamar a la red (`notPermittedCalls`), cayendo directo al fallback.

Se dispara además una notificación asíncrona (`ReservationConfirmationEvent` +
`@TransactionalEventListener(phase = AFTER_COMMIT)` con `@Async`) que simula el envío de un correo
vía log — corre en otro hilo y solo después de que la transacción de confirmación ya commiteó, así
nunca notifica un cambio de estado que después se revierte.

### Modularización de Spring Boot 4 (gotchas encontrados)

Boot 4 partió `spring-boot-autoconfigure` en módulos por feature. Ya nos mordió varias veces
durante el desarrollo — vale la pena dejarlo documentado porque cualquier receta pensada para
Boot 3.x puede fallar en silencio:

- **Flyway**: `flyway-core` solo, sin `spring-boot-starter-flyway`, no auto-configura nada (la
  app arrancaba sin aplicar ninguna migración, sin error visible, hasta que Hibernate fallaba
  validando el esquema).
- **Jackson `ObjectMapper`**: no hay un bean `ObjectMapper` inyectable por defecto aunque la
  serialización JSON normal de los controllers funcione (Spring MVC arma su propio conjunto de
  `HttpMessageConverter` sin publicar el `ObjectMapper` como bean). `RestAuthenticationEntryPoint`
  y `RestAccessDeniedHandler` —que corren fuera del ciclo normal de Spring MVC, dentro del filtro
  de seguridad— no pueden depender de inyectar ese bean; construyen su propio `ObjectMapper` con
  `JavaTimeModule` registrado en vez de `@Autowired`.
- **Actuator `HealthIndicator` se movió de módulo**: en Boot 3.x vivía en
  `org.springframework.boot.actuate.health`, dentro de `spring-boot-actuator`. Boot 4 lo extrajo a
  un módulo nuevo, `spring-boot-health`, con las clases relocadas a
  `org.springframework.boot.health.contributor`. `resilience4j-spring-boot3:2.2.0` (la versión más
  reciente disponible al momento de este proyecto) todavía apunta a la ruta vieja en el
  `@ConditionalOnClass` de su propio health indicator — la condición nunca matchea, así que el
  indicador de resilience4j no se registra y `/actuator/health` no mostraba el estado del circuit
  breaker (aunque `/actuator/circuitbreakers` sí funcionaba, porque esa auto-configuración no
  depende de la API de health). Solución: `CircuitBreakersHealthIndicator` propio, escrito contra
  la API nueva (`org.springframework.boot.health.contributor.HealthIndicator`), que expone el
  estado de cada instancia como detalle sin nunca marcar la app como `DOWN` — un circuito abierto
  es una degradación ya manejada por el fallback, no una falla de la aplicación.

### Resilience4j: `resilience4j-spring-boot3` en vez de Spring Cloud Circuit Breaker

El setup inicial traía `spring-cloud-starter-circuitbreaker-resilience4j` (y el BOM
`spring-cloud-dependencies` solo para gestionar su versión). Se reemplazó por
`io.github.resilience4j:resilience4j-spring-boot3` porque el wrapper de Spring Cloud expone
resilience4j vía `CircuitBreakerFactory` (programático, sin `@CircuitBreaker` declarativo) y **no**
trae los endpoints `/actuator/circuitbreakers` / `/actuator/circuitbreakerevents` — ambos
requeridos explícitamente para esta prueba. El starter nativo de resilience4j sí trae ambos, más
la anotación `@CircuitBreaker` con configuración declarativa en `application.yaml`, que es lo que
se terminó usando en `PaymentValidationService`.

## Testing

```bash
./mvnw test                        # unitarios (Mockito), foco en service layer
./mvnw verify -Pintegration         # integración (@SpringBootTest + Testcontainers)
```

## Progreso

- [x] Setup de proyecto (Spring Boot, dependencias, JDK 21)
- [x] Docker Compose con PostgreSQL
- [x] Perfiles `dev`
- [x] Docker Compose con la app (`Dockerfile` + servicio `app`, perfil `docker`)
- [x] Migraciones Flyway (`V1__init.sql`) con constraint de no-solapamiento a nivel de BD
- [x] Entidades JPA (`User`, `Space`, `Reservation`)
- [x] Repositorios Spring Data JPA (`User`, `Space`, `Reservation`)
- [x] Spring Security + JWT (registro/login, roles `ADMIN`/`USER`, `401` vs `403` consistentes)
- [x] `GlobalExceptionHandler` base (`409` email duplicado, `400` validación, `401`/`403` auth)
- [x] CRUD de espacios (`Space`), consulta abierta a ambos roles, alta/edición/baja solo `ADMIN`
- [x] Máquina de estados (`Reservation`) con patrón State
- [x] Endpoints básicos de reservas (crear, listar/consultar, cancelar)
- [x] Validación de solapamiento de reservas (service + constraint de BD como red de seguridad)
- [x] Circuit breaker sobre validación de pago (mock propio + endpoint de confirmación)
- [x] Notificación asíncrona de reserva (`@Async` + `@TransactionalEventListener`)
- [ ] Endpoint de reportes con `@Cacheable`
- [ ] Perfil `prod`
- [ ] Tests unitarios e integración
- [ ] Colección Postman / `.http`

## Fuera de alcance

- No se requiere frontend.
- No se implementa un generador CRUD genérico; cada endpoint sigue el flujo de capas descrito
  arriba.

(Esta sección se ampliará con los trade-offs concretos tomados durante la implementación de
cada regla de negocio.)

## Qué se haría con más tiempo

(Pendiente — se completa hacia el final de la implementación, cuando el alcance recortado por el
límite de 4 días sea concreto y no una lista genérica.)
