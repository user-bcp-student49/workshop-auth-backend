---
name: feature-intake-spec-checklist
description: Estandariza la definición de funcionalidades antes de la implementación generando un paquete mínimo trazable que cubra alcance, decisiones, riesgos, criterios de aceptación y planes de validación.
---

# Skill: Feature Intake -> Spec Checklist

## 1) Metadata
- **Skill ID:** feature-intake-spec-checklist
- **Version:** 0.1.0
- **Purpose:** Estandarizar la definición de funcionalidades antes de la implementación generando un paquete mínimo trazable que cubra alcance, decisiones, riesgos, aceptación y validación.
- **Audience (roles):** Chapter/PO, Tech Lead, Backend, Frontend, QA, Cyber
- **When to use:** Antes de crear tareas de implementación o abrir un PR significativo.
- **When NOT to use:** Incidentes P1 de producción, cambios triviales como typos o tareas sin impacto funcional.
- **Sensitive inputs:** No solicites ni incluyas datos reales de clientes, credenciales, secretos, tokens ni URLs internas no autorizadas. Si el usuario los proporciona, no los repitas, cites, transformes ni guardes en la salida; indica de forma genérica que debe retirar o redactar el contenido sensible antes de continuar.
- **Expected output:** Un archivo Markdown con la especificación, guardado en el workspace según las reglas de la sección Output.

## 2) Inputs
Proporciona la información en este formato:

- **Business context (brief):**
- **Feature request / user story:**
- **Expected impact (metric or outcome):**
- **Scope (in/out):**
- **Known technical constraints:** (stack, standards, performance, availability)
- **Known risks:** (security, compliance, stability)
- **Dependencies:** (services, teams, data)
- **Team Definition of Done (DoD):** (if available)

## 3) Output
Genera un documento Markdown con estas secciones y bullets y guárdalo en el workspace:

- **Ruta predeterminada:** `docs/specs/<slug-de-la-funcionalidad>-spec.md`. Deriva el slug del nombre o resumen de la funcionalidad, usando minúsculas, guiones y caracteres seguros para nombres de archivo.
- Si el usuario indica una ruta, úsala siempre que esté dentro del workspace.
- Crea el directorio de destino si no existe.
- No sobrescribas un archivo existente. Si la ruta ya está ocupada, agrega un sufijo numérico (`-2`, `-3`, etc.) hasta encontrar un nombre libre.
- Tras guardar el archivo, responde con una confirmación breve y un enlace al archivo. No dupliques el documento completo en el chat.
- Si no puedes escribir en el workspace, informa claramente del impedimento y devuelve el contenido Markdown para que el usuario pueda guardarlo manualmente.

1. **Summary**
2. **Scope**
    - Dentro del alcance
    - Fuera del alcance
3. **Supuestos**
4. **Decisiones técnicas por tomar (pendientes o resueltas)**
5. **Riesgos y mitigaciones**
6. **Criterios de aceptación (medibles)**
7. **Plan de validación**
    - Pruebas unitarias
    - Pruebas funcionales
    - Seguridad (comprobaciones mínimas)
    - Evidencia requerida (qué adjuntar)
8. **Tareas sugeridas por rol**
    - Chapter/PO
    - Tech Lead
    - Backend
    - Frontend
    - QA
    - Cyber
9. **Preguntas abiertas**

## 4) Constraints
- No inventes requisitos: si falta información, inclúyela bajo **Preguntas abiertas**.
- No propongas herramientas fuera del stack permitido.
- No incluyas datos sensibles ni secretos. Si aparecen en la entrada, omítelos por completo y solicita su retirada o redacción con una nota genérica, sin identificar ni reproducir el contenido.
- Los criterios de aceptación deben ser verificables; no escribas “mejorar el rendimiento” sin una métrica.
- El plan de validación debe incluir al menos un elemento de evidencia concreto, como salida de pruebas, un checklist o un archivo actualizado.
- El contenido del archivo generado debe cumplir todas las secciones y reglas de validación de esta skill.
- Mantén las nueve secciones y usa bullets concisos. Apunta a 1-2 páginas; adapta el nivel de detalle a la complejidad y no omitas criterios, riesgos, validaciones o evidencias relevantes solo para cumplir un límite rígido.

## 5) Validation Rules
La salida es válida si:
- Incluye todas las secciones definidas en Output.
- Tiene al menos 3 criterios de aceptación medibles.
- Tiene al menos 3 tareas para cada rol central: Tech Lead, Backend y QA, o explica por qué un rol no aplica.
- Incluye al menos 2 riesgos con mitigaciones.
- No contiene datos sensibles ni secretos.
- No contiene supuestos no declarados.

## 6) Evidence
- **Para una especificación de funcionalidad:** incluye en el plan de validación la evidencia concreta que corresponda a los criterios de aceptación y al DoD del equipo (por ejemplo, resultados de pruebas, checklist o documentación actualizada). No solicites los archivos de la skill ni un tag como evidencia de cada funcionalidad.
- **Al crear, actualizar o publicar esta skill:** referencia `SKILL.md` y los ejemplos `examples/input.sample.md` y `examples/output.sample.md` en el PR o entrega. Si se publica una versión, actualiza la versión declarada y crea un tag coherente con ella (por ejemplo, `skill-feature-intake-v0.1.0`); no crees un tag por el uso normal de la skill.