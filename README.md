# Sistema de Procesamiento de Transacciones Financieras

Sistema backend de procesamiento de transacciones financieras con detección automática de fraude, auditoría completa y reportes. Diseñado con arquitectura segura y calidad de código verificada.

## 📋 Descripción

Plataforma empresarial que procesa transferencias bancarias en tiempo real, implementa algoritmos de detección de fraude y mantiene registro auditable de todas las operaciones. El sistema garantiza seguridad, integridad de datos y compliance normativo en operaciones financieras.

## 🛠️ Stack Tecnológico

| Componente | Tecnología |
|-----------|-----------|
| **Backend** | Java 11+, Spring Boot |
| **Base de Datos** | MySQL |
| **DevOps** | Docker, Docker Compose |
| **CI/CD** | GitHub Actions |
| **Calidad de Código** | SonarQube |
| **Frontend** | Flutter (app mobile companion) |
| **API** | REST APIs |

## 🎯 Funcionalidades Principales

- ✅ **Procesamiento de Transacciones**: Ejecución de transferencias bancarias con validación multi-capa
- ✅ **Detección de Fraude**: Algoritmos de análisis para identificar patrones sospechosos
- ✅ **Auditoría Completa**: Registro exhaustivo de todas las operaciones y cambios
- ✅ **Reportes**: Generación de reportes financieros y operacionales
- ✅ **Seguridad de Código**: Análisis con SonarQube para garantizar estándares de seguridad
- ✅ **Integración Continua**: Pipeline CI/CD automatizado con GitHub Actions
- ✅ **Containerización**: Deployment mediante Docker para portabilidad

## 🚀 Cómo Ejecutarlo

### Requisitos Previos
- Java 11 o superior
- Maven 3.6+
- Docker y Docker Compose
- MySQL 8.0+ (o incluido en docker-compose)

### Instalación y Ejecución

**1. Clonar el repositorio:**
```bash
git clone https://github.com/rodrrigg0/Sistema-de-Procesamiento-de-Transacciones-Financieras---SEIDOR.git
cd Sistema-de-Procesamiento-de-Transacciones-Financieras---SEIDOR
```

**2. Opción A: Con Docker Compose (recomendado):**
```bash
docker-compose up -d
```
La aplicación estará disponible en: `http://localhost:8080`

**3. Opción B: Con Maven:**
```bash
mvn clean install
mvn spring-boot:run
```

### Acceso a la Aplicación
- **API REST**: http://localhost:8080/api
- **Swagger/Docs**: http://localhost:8080/swagger-ui.html (si configurado)

## 📊 Análisis de Calidad de Código

El código se valida continuamente con **SonarQube** para garantizar:
- Seguridad (vulnerabilidades críticas)
- Mantenibilidad (complejidad ciclomática)
- Fiabilidad (errores potenciales)
- Coverage de tests

## 🔄 CI/CD Pipeline

El proyecto implementa integración continua con **GitHub Actions** que:
1. Ejecuta tests automáticos en cada push
2. Construye la imagen Docker
3. Ejecuta análisis de calidad con SonarQube
4. Valida seguridad del código

## 📚 Arquitectura

- **Patrón**: Arquitectura en capas (Controller → Service → Repository)
- **Seguridad**: Validación de fraude con análisis de patrones
- **Persistencia**: JPA/Hibernate con MySQL
- **Testing**: JUnit + Mockito

## 👨‍💻 Autor

**Rodrigo Bachiller Sanz** - Junior Backend Developer  
Experiencia: Spring Boot, Docker, MySQL, Architecture Design

## 📄 Licencia

Proyecto educativo / profesional

---

**Nota**: Este proyecto fue desarrollado durante experiencia laboral en Seidor Consulting como parte de formación en desarrollo backend con Spring Boot y arquitectura empresarial.
