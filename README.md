# Paolicias.Sweet — MVP móvil de cotización y reserva de eventos

App Android (Kotlin + Jetpack Compose + Material 3) para la gestión, cotización y reserva de eventos de
pastelería fina y banquetería. Proyecto de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles**
(Evaluación Parcial 2).

## Integrantes
- Tomás Gonzalez
- Matías Obreque
- Matías Noriega

## Funcionalidades
**Cliente**
- Registro e inicio de sesión con formularios validados (nombre, correo, teléfono, empresa opcional, contraseña).
- Catálogo de tortas y menús de banquetería con filtro por categoría.
- Cotización en tiempo real: fecha, horario, asistentes (10 a 100) y observaciones (alergias, restricciones).
- Reserva con abono obligatorio del 50 % y estados: *pendiente → comprobante en revisión → abono 50 % pagado → completado*.
- Adjuntar el comprobante de transferencia con **cámara** o **galería**.
- Indicar el lugar del evento con **ubicación (GPS)**.

**Administradora**
- Agenda operativa cronológica con filtro por estado.
- Detalle de cada pedido: contacto, menú, asistentes, observaciones, comprobante y ubicación.
- Validar o rechazar el comprobante del abono y marcar eventos completados.
- Estimación consolidada de personal (garzones y ayudantes de cocina) para eventos con abono validado.

## Reglas de negocio
- Tarifario escalonado (datos sintéticos): se cobra por tramos de 10 personas, tope 100.
  Descuento por volumen: hasta 20 personas 0 %, hasta 50 personas 5 %, hasta 100 personas 10 %.
- El abono del 50 % bloquea la fecha: no se puede reservar una fecha y horario que ya tengan un evento con abono validado.
- Dotación estimada: 1 garzón cada 20 asistentes y 1 ayudante de cocina cada 40.
- Todos los datos son ficticios (Ley 19.628 y Ley 21.719).

## Requisitos de la evaluación
| Requisito | Dónde está |
|---|---|
| MVVM | `viewmodel/`, `data/`, `ui/` |
| Formularios validados con íconos y mensajes | `ui/components/CampoTexto.kt`, `AuthScreen`, `CotizarScreen` |
| Validaciones desacopladas de la UI | `domain/Validaciones.kt` y `domain/Cotizador.kt` |
| Persistencia local | Room: `data/` (usuarios, productos, reservas) |
| Navegación | `ui/AppNav.kt` (Navigation Compose, con argumentos y transiciones) |
| Animaciones | `AnimatedVisibility`, `AnimatedContent`, `animateContentSize`, `animateItem`, `animateIntAsState`, transiciones de navegación |
| Recursos nativos | Cámara, galería y ubicación (`recursos/`) |
| Pruebas unitarias | `app/src/test/` (cotizador y validaciones) |

## Cuenta de administradora (ficticia)
- Correo: `admin@paoliciassweet.cl`
- Contraseña: `Admin123`

Los usuarios que se registran desde la app siempre tienen rol cliente.

## Cómo ejecutar
1. Abrir la carpeta `PaoliciasSweet` (la que contiene `app` y `settings.gradle.kts`) en Android Studio.
2. Si pide el JVM de Gradle, elegir **JDK 17 o 21**.
3. Esperar la sincronización de Gradle.
4. Ejecutar en un emulador o dispositivo con Android 8.0+ (API 26).
5. Aceptar los permisos de cámara y ubicación cuando se soliciten.

## Flujo de trabajo
- Repositorio con **GitHub Flow**: rama `main` protegida, una rama por tarea (`feature/...`) y Pull Request.
- Planificación en Trello: [enlace al tablero]

## Estructura
```
app/src/main/java/cl/duoc/paoliciassweet/
├── data/        Room (entidades, DAOs, base de datos), repositorio y sesión
├── domain/      Validaciones, cotizador y formato
├── viewmodel/   ViewModels con StateFlow
├── recursos/    Cámara (FileProvider) y ubicación
└── ui/          Pantallas Compose, navegación y componentes
```
