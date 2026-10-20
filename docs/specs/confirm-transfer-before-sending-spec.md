# Especificación: Confirmación previa al envío de transferencias

## 1. Summary
- Incorporar una confirmación previa al envío con un resumen y validación de datos, con el objetivo de reducir en un 20 % las quejas por transferencias duplicadas durante un mes.

## 2. Scope
- **Dentro del alcance**
  - Mostrar una confirmación antes de enviar una transferencia.
  - Mostrar un resumen de la transferencia.
  - Validar los datos de la transferencia.
- **Fuera del alcance**
  - Cambios de infraestructura.
  - Rediseño completo de la interfaz.

## 3. Supuestos
- Se utilizarán la API REST, el frontend web y los estándares de logging existentes.
- La integración depende de los servicios de cuentas y transferencias.
- Las reglas concretas de validación y los campos del resumen aún deben acordarse.

## 4. Decisiones técnicas por tomar (pendientes o resueltas)
- **Pendiente:** acordar con los servicios de cuentas y transferencias el contrato y flujo para confirmar y enviar.
- **Pendiente:** definir los campos que mostrará el resumen.
- **Pendiente:** establecer las reglas de validación del monto y de los demás datos.
- **Pendiente:** definir la fuente y línea base para medir las quejas por duplicados.
- **Resuelto:** mantener la API, el frontend y los estándares de logging existentes; no realizar cambios de infraestructura ni un rediseño completo.

## 5. Riesgos y mitigaciones
- **Exposición de datos sensibles en logs:** revisar los eventos del flujo y comprobar que no registren valores sensibles.
- **Errores en la validación del monto:** acordar las reglas y límites con los responsables del servicio y cubrirlos con pruebas.
- **Contrato inconsistente entre frontend y servicios:** acordar el contrato de integración antes de implementar y probar el flujo integrado.

## 6. Criterios de aceptación (medibles)
- En todos los intentos válidos, la confirmación y el resumen se muestran antes de enviar la transferencia.
- En todos los casos con datos inválidos según las reglas acordadas, el envío queda bloqueado y se presenta un mensaje de validación.
- Los logs generados por el flujo contienen cero valores sensibles de la transferencia.
- Durante el primer mes, las quejas por transferencias duplicadas disminuyen un 20 % frente a la línea base acordada.

## 7. Plan de validación
- **Pruebas unitarias**
  - Cubrir las reglas acordadas para datos válidos, inválidos y límites del monto.
  - Verificar que la confirmación se presente antes de invocar el envío.
- **Pruebas funcionales**
  - Verificar el recorrido con datos válidos, desde el resumen hasta el envío confirmado.
  - Verificar que datos inválidos bloqueen el envío y muestren un mensaje.
  - Probar la integración con los servicios de cuentas y transferencias.
- **Seguridad (comprobaciones mínimas)**
  - Inspeccionar los logs del flujo y confirmar que no contienen valores sensibles.
  - Verificar que el envío no pueda continuar cuando falle la validación.
- **Evidencia requerida**
  - Resultados de pruebas unitarias y funcionales.
  - Checklist de revisión de datos sensibles en logs.
  - Medición de quejas con la línea base, periodo y resultado del primer mes.

## 8. Tareas sugeridas por rol
- **Chapter/PO**
  - Acordar la línea base, fuente y periodo de medición de quejas.
  - Definir el contenido esperado del resumen.
  - Revisar el resultado de la métrica al finalizar el primer mes.
- **Tech Lead**
  - Acordar el contrato de integración con los servicios dependientes.
  - Definir con los responsables las reglas de validación.
  - Revisar el diseño técnico y el PR.
- **Backend**
  - Implementar o ajustar el flujo REST de confirmación y envío.
  - Aplicar las reglas de validación acordadas antes del envío.
  - Comprobar que el logging no exponga datos sensibles y actualizar la documentación breve.
- **Frontend**
  - Implementar la confirmación previa y el resumen.
  - Mostrar errores de validación y bloquear el envío cuando corresponda.
  - Integrar el recorrido con la API existente.
- **QA**
  - Probar casos válidos, inválidos y límites acordados.
  - Verificar que no se envíe una transferencia antes de la confirmación.
  - Adjuntar resultados de pruebas y checklist de logs.
- **Cyber**
  - Revisar el riesgo de exposición de datos en logs.
  - Verificar que el flujo respete las validaciones antes del envío.
  - Registrar los resultados de las comprobaciones de seguridad.

## 9. Preguntas abiertas
- ¿Cuál es la línea base y qué fuente se usará para medir las quejas por duplicados?
- ¿Qué campos debe contener el resumen de confirmación?
- ¿Cuáles son las reglas y límites válidos para el monto y los demás datos?
- ¿Qué contrato de API y flujo de confirmación acordarán los servicios de cuentas y transferencias?
