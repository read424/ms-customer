# Code Coverage Report - JaCoCo

## Overview

Este proyecto utiliza **JaCoCo (Java Code Coverage)** para medir la cobertura de código de las pruebas unitarias e integración.

## Configuración

JaCoCo está configurado en `pom.xml` con las siguientes características:

- **Plugin**: `jacoco-maven-plugin` v0.8.11
- **Mínimo de cobertura requerido**: 50% por package (excepto generated code)
- **Reportes generados**: HTML, XML, CSV
- **Fases del build**: Initialize → Test → Verify

## Ejecución

### Generar cobertura + Reporte

```bash
mvn clean test
```

Esto ejecutará:
1. Tests con instrumentación de JaCoCo
2. Generación automática de `target/jacoco.exec`
3. Reporte HTML en `target/site/jacoco/`

### Solo generar reporte (sin tests)

```bash
mvn jacoco:report
```

## Visualizar Reporte

Abrir el reporte generado:

```bash
# Linux/Mac
open target/site/jacoco/index.html

# Windows
start target/site/jacoco/index.html
```

## Estructura de Reportes

```
target/site/jacoco/
├── index.html              # Resumen general
├── jacoco.csv             # Datos en formato CSV
├── jacoco.xml             # Datos en formato XML
├── jacoco-sessions.html   # Detalles de sesión
└── [packages]/            # Reportes por package
```

## Métricas de Cobertura

Las siguientes métricas se miden:

- **INSTRUCTION_COVERED**: Instrucciones ejecutadas
- **BRANCH_COVERED**: Ramas if/else ejecutadas
- **LINE_COVERED**: Líneas ejecutadas
- **METHOD_COVERED**: Métodos ejecutados
- **COMPLEXITY_COVERED**: Complejidad ciclomática cubierta

## Exclusiones Actuales

Packages excluidos del reporte:

- `com.bootcamp.customer.*` (Generated code from OpenAPI)
- `com.bootcamp.ms_customer.infrastructure.config` (Configuration code)

## Minimo de Cobertura

- **PACKAGE level**: 50% de cobertura de líneas (deshabilitado con `haltOnFailure=false`)
- **Verificación**: `mvn verify` valida las reglas de cobertura

## Mejorando la Cobertura

Para aumentar la cobertura de código:

1. **Agregar tests unitarios** para lógica de negocio
2. **Agregar tests de integración** para adapters
3. **Evitar código muerto** (refactorizar si no se usa)
4. **Aumentar límites** en `pom.xml` bajo `<rules>` si es justificado

## Ejemplo: Agregar Test

```java
@Test
@DisplayName("Should create customer successfully")
void testCreateCustomer() {
    // Arrange
    CreateCustomerDto dto = new CreateCustomerDto();
    dto.setFirstName("John");
    
    // Act
    Mono<Customer> result = service.createCustomer(dto);
    
    // Assert
    StepVerifier.create(result)
        .assertNext(customer -> assertThat(customer.getFirstName()).isEqualTo("John"))
        .verifyComplete();
}
```

## CI/CD Integration

Para integrar en CI/CD:

```yaml
# GitHub Actions Example
- name: Run tests with coverage
  run: mvn clean test

- name: Upload coverage
  uses: codecov/codecov-action@v3
  with:
    files: ./target/site/jacoco/jacoco.xml
```

## Referencias

- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
- [Coverage Goals](https://www.jacoco.org/jacoco/trunk/doc/maven.html#Coverage_Goals)
