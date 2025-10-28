# Guía de Contribución

¡Gracias por tu interés en contribuir a FlexUI! Este documento proporciona pautas y mejores prácticas para contribuir al proyecto.

## 📋 Tabla de Contenidos

- [Código de Conducta](#código-de-conducta)
- [Cómo Contribuir](#cómo-contribuir)
- [Proceso de Desarrollo](#proceso-de-desarrollo)
- [Estándares de Código](#estándares-de-código)
- [Commits y Pull Requests](#commits-y-pull-requests)
- [Reportar Bugs](#reportar-bugs)
- [Solicitar Features](#solicitar-features)

## 🤝 Código de Conducta

Este proyecto se adhiere a un código de conducta. Al participar, se espera que mantengas un ambiente respetuoso y profesional.

## 🚀 Cómo Contribuir

### 1. Fork y Clone

```bash
# Fork el repositorio en GitHub
# Luego clona tu fork
git clone https://github.com/tu-usuario/flex-ui-components.git
cd flex-ui-components
```

### 2. Configura el Upstream

```bash
git remote add upstream https://github.com/original-owner/flex-ui-components.git
git fetch upstream
```

### 3. Crea una Rama

```bash
# Para features
git checkout -b feature/nombre-descriptivo

# Para bugs
git checkout -b fix/nombre-del-bug

# Para documentación
git checkout -b docs/descripcion
```

## 🔄 Proceso de Desarrollo

### Configuración del Entorno

1. **Requisitos:**
   - JDK 17 o superior
   - Android Studio Hedgehog o superior
   - Gradle 8.13.0+

2. **Instalación:**
   ```bash
   ./gradlew build
   ```

3. **Ejecutar Tests:**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

### Workflow de Desarrollo

1. **Sincroniza tu fork:**
   ```bash
   git fetch upstream
   git checkout main
   git merge upstream/main
   ```

2. **Desarrolla tu feature:**
   - Escribe código siguiendo los estándares
   - Agrega tests para nueva funcionalidad
   - Actualiza documentación si es necesario

3. **Ejecuta verificaciones locales:**
   ```bash
   ./gradlew lint
   ./gradlew test
   ./gradlew build
   ```

4. **Commit y Push:**
   ```bash
   git add .
   git commit -m "feat: descripción del cambio"
   git push origin feature/nombre-descriptivo
   ```

5. **Crea Pull Request:**
   - Ve a GitHub y crea un PR desde tu rama
   - Completa el template de PR
   - Espera revisión

## 📝 Estándares de Código

### Kotlin Style Guide

Seguimos las [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) y nuestros [Android Kotlin Standards](.kiro/steering/android-kotlin-standards.md).

**Puntos clave:**

- **Nombres:**
  - Clases: `PascalCase`
  - Funciones/Variables: `camelCase`
  - Constantes: `UPPER_SNAKE_CASE`
  - Composables: `PascalCase`

- **Formato:**
  - Indentación: 4 espacios
  - Línea máxima: 120 caracteres
  - Imports ordenados y agrupados

- **Documentación:**
  - KDoc para APIs públicas
  - Comentarios para lógica compleja
  - Ejemplos de uso cuando sea apropiado

### Jetpack Compose

- Modifier siempre como último parámetro con valor por defecto
- Usar `remember` para estado local
- Usar `LaunchedEffect` para side effects
- Keys estables en listas
- Evitar crear lambdas en el cuerpo del composable

### Testing

- **Unit Tests:** Para lógica de negocio y transformaciones
- **Compose Tests:** Para UI y interacciones
- **Cobertura mínima:** 70% para nuevo código
- **Nombres descriptivos:** `should_returnSuccess_when_jsonIsValid()`

Ejemplo:

```kotlin
@Test
fun `parse should return success when JSON is valid`() = runBlocking {
    // Arrange
    val validJson = """{"id": "test", "type": "componentButton"}"""
    val parser = JsonParser()
    
    // Act
    val result = parser.parse(validJson)
    
    // Assert
    assertTrue(result.isSuccess)
    assertEquals("test", result.getOrNull()?.id)
}
```

## 💬 Commits y Pull Requests

### Formato de Commits

Usamos [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types:**
- `feat`: Nueva funcionalidad
- `fix`: Corrección de bug
- `docs`: Cambios en documentación
- `style`: Formato, punto y coma faltantes, etc.
- `refactor`: Refactorización de código
- `test`: Agregar o modificar tests
- `chore`: Tareas de mantenimiento

**Ejemplos:**

```bash
feat(parser): add support for custom component types
fix(renderer): resolve memory leak in image loading
docs(readme): update installation instructions
test(components): add tests for ButtonComponent
```

### Pull Requests

1. **Usa el template de PR** proporcionado
2. **Título descriptivo:** Sigue el formato de conventional commits
3. **Descripción completa:** Explica el contexto y la solución
4. **Tests:** Incluye tests para cambios de código
5. **Screenshots:** Para cambios de UI
6. **Checklist:** Completa todos los items aplicables

**Criterios de Aprobación:**

- ✅ Pasa todos los checks de CI
- ✅ Revisión aprobada por al menos un maintainer
- ✅ Tests con cobertura adecuada
- ✅ Documentación actualizada
- ✅ Sin conflictos con main

## 🐛 Reportar Bugs

Usa el [template de bug report](.github/ISSUE_TEMPLATE/bug_report.md) e incluye:

- Descripción clara del problema
- Pasos para reproducir
- Comportamiento esperado vs actual
- Entorno (dispositivo, Android version, etc.)
- Screenshots o logs si es posible

## ✨ Solicitar Features

Usa el [template de feature request](.github/ISSUE_TEMPLATE/feature_request.md) e incluye:

- Descripción de la funcionalidad
- Problema que resuelve
- Solución propuesta
- Alternativas consideradas
- Mockups o ejemplos si aplica

## 📚 Recursos Adicionales

- [README.md](README.md) - Documentación principal
- [Design Document](.kiro/specs/json-compose-renderer/design.md) - Arquitectura
- [Requirements](.kiro/specs/json-compose-renderer/requirements.md) - Requisitos
- [Android Kotlin Standards](.kiro/steering/android-kotlin-standards.md) - Estándares de código

## 🙏 Reconocimientos

Todos los contribuidores serán reconocidos en el proyecto. ¡Gracias por tu tiempo y esfuerzo!

## ❓ Preguntas

Si tienes preguntas, puedes:

- Abrir una [Discussion](https://github.com/yourusername/flex-ui-components/discussions)
- Crear un [Issue](https://github.com/yourusername/flex-ui-components/issues)
- Contactar a los maintainers

---

**¡Feliz coding! 🚀**
