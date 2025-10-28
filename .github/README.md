# GitHub Configuration

Este directorio contiene la configuración de GitHub para el proyecto FlexUI.

## 📁 Estructura

```
.github/
├── ISSUE_TEMPLATE/
│   ├── bug_report.md          # Template para reportar bugs
│   ├── feature_request.md     # Template para solicitar features
│   ├── documentation.md       # Template para issues de documentación
│   └── config.yml            # Configuración de templates
├── workflows/
│   └── pr-checks.yml         # CI/CD para Pull Requests
├── labels.yml                # Definición de labels del proyecto
├── pull_request_template.md  # Template para Pull Requests
└── README.md                 # Este archivo

```

## 🎯 Templates de Issues

### Bug Report
Usa este template cuando encuentres un bug o comportamiento inesperado.

**Incluye:**
- Descripción del bug
- Pasos para reproducir
- Comportamiento esperado vs actual
- Información del entorno
- Screenshots/logs

### Feature Request
Usa este template para proponer nuevas funcionalidades.

**Incluye:**
- Descripción de la feature
- Problema que resuelve
- Solución propuesta
- Alternativas consideradas
- Criterios de aceptación

### Documentation
Usa este template para reportar documentación faltante o incorrecta.

**Incluye:**
- Ubicación de la documentación
- Estado actual
- Estado deseado
- Contenido sugerido

## 📝 Template de Pull Request

El template de PR incluye secciones para:

- **Business Description**: Contexto de negocio
- **Solution Description**: Descripción técnica de la solución
- **Unit Test Cases**: Casos de prueba cubiertos
- **Screenshots/Videos**: Evidencia visual (si aplica)
- **Pre-Merge Checklist**: Verificaciones antes de merge
- **Additional Notes**: Información adicional

## 🔄 Workflows de CI/CD

### PR Checks (`pr-checks.yml`)

Se ejecuta automáticamente en cada Pull Request y realiza:

1. **Lint Check**: Verifica el código con Android Lint
2. **Unit Tests**: Ejecuta todos los tests unitarios
3. **Build Check**: Compila el proyecto completo
4. **Code Quality**: Ejecuta análisis con Detekt (opcional)

**Requisitos:**
- Todas las verificaciones deben pasar para hacer merge
- Los reportes se suben como artifacts

## 🏷️ Sistema de Labels

### Importar Labels

```bash
# Usando GitHub CLI
gh label create --file .github/labels.yml

# O importar manualmente desde GitHub UI
```

### Categorías de Labels

**Type** (Tipo de cambio):
- `type: feature` - Nueva funcionalidad
- `type: bug` - Corrección de bug
- `type: documentation` - Documentación
- `type: refactor` - Refactorización
- `type: test` - Tests
- `type: chore` - Mantenimiento

**Priority** (Prioridad):
- `priority: critical` - Crítico
- `priority: high` - Alta
- `priority: medium` - Media
- `priority: low` - Baja

**Status** (Estado):
- `status: in progress` - En progreso
- `status: blocked` - Bloqueado
- `status: needs review` - Necesita revisión
- `status: needs testing` - Necesita testing
- `status: ready to merge` - Listo para merge

**Component** (Componente):
- `component: parser` - Parser JSON
- `component: renderer` - Renderizador
- `component: styling` - Sistema de estilos
- `component: events` - Sistema de eventos
- `component: containers` - Contenedores
- `component: atomic` - Componentes atómicos

**Size** (Tamaño del cambio):
- `size: XS` - Extra pequeño (< 10 líneas)
- `size: S` - Pequeño (10-50 líneas)
- `size: M` - Mediano (50-200 líneas)
- `size: L` - Grande (200-500 líneas)
- `size: XL` - Extra grande (> 500 líneas)

**Special** (Especiales):
- `good first issue` - Bueno para principiantes
- `help wanted` - Se necesita ayuda
- `breaking change` - Cambio que rompe compatibilidad
- `dependencies` - Actualización de dependencias
- `security` - Relacionado con seguridad
- `performance` - Mejora de rendimiento
- `accessibility` - Mejora de accesibilidad

## 🚀 Uso Recomendado

### Para Contribuidores

1. **Crear Issue:**
   - Usa el template apropiado
   - Agrega labels relevantes
   - Asígnate si vas a trabajar en ello

2. **Crear Pull Request:**
   - Completa todas las secciones del template
   - Referencia el issue relacionado
   - Agrega labels apropiados
   - Solicita revisión

3. **Durante la Revisión:**
   - Responde a comentarios
   - Actualiza el código según feedback
   - Mantén el PR actualizado con main

### Para Reviewers

1. **Revisar PR:**
   - Verifica que el template esté completo
   - Revisa el código según estándares
   - Ejecuta localmente si es necesario
   - Verifica que pasen los checks de CI

2. **Aprobar o Solicitar Cambios:**
   - Deja comentarios constructivos
   - Aprueba cuando esté listo
   - Actualiza labels según estado

## 📚 Recursos

- [CONTRIBUTING.md](../CONTRIBUTING.md) - Guía de contribución
- [README.md](../README.md) - Documentación principal
- [Android Kotlin Standards](../.kiro/steering/android-kotlin-standards.md) - Estándares de código

## 🔧 Mantenimiento

### Actualizar Templates

Los templates se pueden actualizar editando los archivos en este directorio. Los cambios se aplicarán automáticamente a nuevos issues y PRs.

### Actualizar Workflows

Los workflows se pueden modificar editando los archivos YAML en `workflows/`. Los cambios requieren un commit para aplicarse.

### Actualizar Labels

Para actualizar labels:

1. Edita `labels.yml`
2. Ejecuta: `gh label create --file .github/labels.yml`
3. O actualiza manualmente desde GitHub UI

---

**Última actualización:** 2025-10-28
