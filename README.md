# Customer Microservice (ms-customer)

Este es el microservicio encargado de la gestión de clientes dentro del ecosistema bancario, desarrollado en base a los requerimientos del bootcamp de NTT Data.

## Arquitectura y Patrones de Diseño

El microservicio ha sido diseñado e implementado con un enfoque robusto y escalable, aplicando los siguientes principios y patrones:

### Arquitectura Hexagonal (Puertos y Adaptadores)
El proyecto está estructurado en base a la **Arquitectura Hexagonal**, asegurando que la lógica de negocio (dominio) sea completamente independiente de frameworks y tecnologías externas.

La estructura de paquetes refleja este enfoque:
- **`domain`**: Contiene el corazón de la aplicación. Aquí se encuentran los modelos de dominio, enumeradores, excepciones personalizadas y los *Domain Services*. Esta capa no tiene dependencias de Spring u otras librerías externas (aislamiento).
- **`application`**: Coordina los casos de uso. Contiene los **Puertos de Entrada** (interfaces para ser usadas por adaptadores inbound) y los **Puertos de Salida** (interfaces que la infraestructura debe implementar, como repositorios).
- **`infrastructure`**: Contiene los adaptadores tecnológicos.
    - **`inbound`**: Adaptadores de entrada (Controladores REST generados por OpenAPI, Mappers).
    - **`outbound`**: Adaptadores de salida (Repositorios Spring Data MongoDB, Entidades de base de datos, Mappers).
    - **`config`**: Configuraciones de Spring, beans, propiedades externas.

### Domain-Driven Design (DDD)
Se han aplicado principios de DDD para modelar la solución:
- **Entidades y Agregados**: Se modelan los conceptos del negocio como *Customer* diferenciando entre los perfiles **Personal** (Estándar, VIP) y **Empresarial** (Estándar, PYME).
- **Separación de Modelos**: Existen clases separadas para el dominio (`Customer`), la persistencia (`CustomerEntity` en infraestructura) y la transferencia de datos (DTOs generados por OpenAPI). El mapeo entre estas capas se realiza mediante **MapStruct**.

### Patrones de Diseño Implementados
- **Dependency Injection (Inyección de Dependencias)**: A través de constructores, promoviendo la inmutabilidad y facilitando el testing.
- **Repository Pattern**: Abstracción del acceso a datos mediante Spring Data Reactive MongoDB.
- **Adapter Pattern**: En los puertos inbound y outbound para comunicar el dominio con el exterior.
- **Circuit Breaker**: Implementado con **Resilience4j** para dotar de tolerancia a fallos a las comunicaciones (timeout de 2s).
- **Database per Service**: El microservicio posee su propia base de datos (MongoDB), asegurando el bajo acoplamiento.

## Tecnologías y Herramientas

- **Java 17**
- **Spring Boot 3.x**
- **Spring WebFlux / Project Reactor**: Programación reactiva (flujos no bloqueantes).
- **Spring Data MongoDB Reactive**: Persistencia NoSQL asíncrona.
- **OpenAPI Generator**: Diseño API-First y generación automática de interfaces REST (contratos).
- **MapStruct & Lombok**: Generación automática de mappers y reducción de código repetitivo (Boilerplate).
- **Resilience4j**: Patrón Circuit Breaker.
- **Spring Cloud (Eureka Client, Config Client)**: Para el registro/descubrimiento del servicio y la externalización de configuraciones.
- **Micrometer & Prometheus**: Observabilidad y métricas.
- **JUnit 5 & Jacoco**: Pruebas unitarias y métricas de cobertura de código (configurado para asegurar un porcentaje mínimo del 50%).
- **Logback**: Manejo de trazas y logs.

## Reglas de Negocio (Dominio)

El microservicio contempla la validación y gestión de las siguientes reglas especificadas en los requerimientos:
1. **Tipos de Clientes**: Personal o Empresarial.
2. **Perfiles Específicos**:
    - **Personal VIP**: Requiere tener tarjeta de crédito y mantiene reglas de montos mínimos.
    - **Empresarial PYME**: Requiere tarjeta de crédito y utiliza cuenta corriente sin comisión.
3. Validación estricta para asegurar que el sistema es consistente respecto a quién puede tener qué productos (ej. un cliente empresarial no puede tener una cuenta de ahorro, un cliente personal solo puede tener un máximo de una cuenta de ahorro/corriente, etc.). *Nota: La interacción con productos se maneja a través de las reglas establecidas para el perfil del cliente.*

## Configuración y Ejecución

1. **Dependencias**: Asegúrese de contar con Java 17 y Maven instalados, además de tener en ejecución (Docker/Local) las siguientes piezas del ecosistema:
    - Config Server
    - Eureka Server
    - MongoDB (Base de datos local o remota configurada).

2. **Compilación y Generación de Clases**:
   El proyecto utiliza OpenAPI y MapStruct en la fase de compilación. Para generar todo el código necesario:
   ```bash
   mvn clean install
   ```

3. **Ejecución**:
   ```bash
   mvn spring-boot:run
   ```

## Cobertura y Calidad de Código
El proyecto utiliza **Jacoco** para generar reportes de cobertura. Al ejecutar los tests con `mvn test`, los reportes HTML se generan en la carpeta `target/site/jacoco/index.html`. También se utiliza el plugin de Checkstyle (según el requerimiento) para garantizar los estándares de código.

---
*Documentación generada considerando los requerimientos del Proyecto Bootcamp (Fases I, II y III).*
