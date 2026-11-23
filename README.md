# Paolicias.Sweet — App móvil de cotización y reserva de eventos

App Android (Kotlin + Jetpack Compose + Material 3) para la gestión, cotización y reserva de eventos de pastelería fina y banquetería, conectada a microservicios Spring Boot propios y a una API externa.
Proyecto de **DSY1105 – Desarrollo de Aplicaciones Móviles** (Evaluación Parcial 4) · Sección 012D.

- **Backend (microservicios):** repositorio `paoliciassweet-backend`
- **Planificación:** Trello → https://trello.com/b/HEVoE6OG/paoliciassweet

## Integrantes
- Tomás González
- Matías Obreque
- Matías Noriega

## Funcionalidades
**Cliente**
- Registro e inicio de sesión con formularios validados.
- Catálogo de tortas y menús de banquetería (desde el microservicio, con respaldo local en Room) y filtro por categoría.
- **Indicadores económicos del día (UF y dólar)** desde la API externa mindicador.cl, mostrados en el catálogo.
- Cotización en tiempo real: fecha, horario, asistentes (10 a 100) y observaciones.
- Reserva con abono obligatorio del 50 % y estados: *pendiente → comprobante en revisión → abono 50 % pagado → completado*.
- **CRUD de reservas:** crear, listar, actualizar estado y **cancelar (eliminar)** la reserva.
- Adjuntar comprobante de transferencia con cámara o galería (se guarda en PostgreSQL vía `comprobantes-service`).
- Indicar el lugar del evento con GPS.

**Administradora**
- Agenda operativa con filtro por estado, validación/rechazo del abono y cierre de eventos.
- Estimación de personal (garzones y ayudantes) para eventos con abono validado.

Cuenta de administradora (ficticia): `admin@paoliciassweet.cl` / `Admin123`.

## Endpoints utilizados

### API externa (Retrofit)
| Método | URL | Uso |
| :--- | :--- | :--- |
| `GET` | `https://mindicador.cl/api` | UF, dólar, euro y UTM del día (sin API key) |

### Microservicios propios (Retrofit)
| Servicio | Método | Ruta |
| :--- | :--- | :--- |
| Catálogo (`:8081`) | `GET` | `/api/productos` |
| Catálogo | `POST` | `/api/cotizaciones` |
| Reservas (`:8082`) | `GET` | `/api/reservas?email=` · `/api/reservas/{id}` |
| Reservas | `POST` | `/api/reservas` |
| Reservas | `PUT` | `/api/reservas/{id}` · `/api/reservas/{id}/estado` |
| Reservas | `DELETE` | `/api/reservas/{id}` |
| Comprobantes (`:8083`) | `POST` | `/api/comprobantes` (multipart) |
| Comprobantes | `GET` | `/api/comprobantes/reserva/{reservaId}` |

Detalle completo en el README del repositorio backend.

## Pasos para ejecutar
1. Abrir la carpeta del proyecto (la que contiene `app/` y `settings.gradle.kts`) en Android Studio, con **JDK 17 o 21**, y esperar la sincronización de Gradle.
2. **Backend en la nube (por defecto en `release`):** las URLs de Render están en `gradle.properties` (`*UrlCloud`).
3. **Backend local (por defecto en `debug`, emulador):** en el repositorio backend ejecutar `docker compose up -d --build`. El emulador accede por `10.0.2.2`. En un teléfono físico, cambiar `*UrlLocal` en `gradle.properties` por la IP del computador.
4. Ejecutar en un emulador o dispositivo con Android 8.0+ (API 26) y aceptar los permisos de cámara y ubicación.

## Pruebas unitarias
```
./gradlew testDebugUnitTest                      # ejecuta las pruebas
./gradlew createDebugUnitTestCoverageReport      # informe de cobertura (JaCoCo)
```
JUnit 4 + MockK + kotlinx-coroutines-test. Cubren `RemoteRepositorio` (acceso a microservicios y API externa), mapeadores DTO→entidad, `Cotizador`, `Validaciones`, indicadores, sesión, estados y utilidades de formato.

## APK firmado y llave (.jks)
- Llave: `keystore/paoliciassweet.jks` (alias `paoliciassweet`; credenciales de demostración en `keystore.properties`).
- Firma configurada en `app/build.gradle.kts` (`signingConfigs.release`).
- Generar el APK: `./gradlew assembleRelease` → `app/build/outputs/apk/release/app-release.apk`; copiarlo a `release/PaoliciasSweet.apk`.
- Verificar la firma: `apksigner verify --verbose release/PaoliciasSweet.apk`

![Captura del APK firmado y .jks](docs/captura-apk-jks.png)

## Flujo de trabajo
GitHub Flow: rama `main`, una rama por tarea (`feature/...`) y Pull Request, con commits de cada integrante. Tareas distribuidas en Trello.

## Estructura
```
app/src/main/java/cl/duoc/paoliciassweet/
├── data/        Room (entidades, DAOs, base de datos), repositorio local y sesión
├── network/     Retrofit (microservicios + API externa), DTOs, mapeadores y RemoteRepositorio
├── domain/      Validaciones, cotizador, indicadores y formato
├── viewmodel/   ViewModels con StateFlow
├── recursos/    Cámara (FileProvider) y ubicación
└── ui/          Pantallas Compose, navegación y componentes
```

Todos los datos son ficticios (Ley 19.628 y Ley 21.719).
