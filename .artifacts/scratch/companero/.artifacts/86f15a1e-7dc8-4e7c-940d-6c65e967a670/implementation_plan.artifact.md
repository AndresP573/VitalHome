# Mejora del Botón SOS y Corrección de Configuración

Este plan aborda el problema del color incorrecto en el botón SOS, mejora su diseño visual para que parezca un botón de emergencia real y corrige la duplicación de iconos en el menú de aplicaciones.

## User Review Required

> [!IMPORTANT]
> Se eliminará el filtro de "LAUNCHER" de `AlertActivity` en el `AndroidManifest.xml`. Esto significa que la aplicación solo tendrá un icono de inicio (que abre la pantalla principal). Si deseas tener un acceso directo separado para las alertas, por favor házmelo saber.

## Proposed Changes

### [UI/UX]

#### [MODIFY] [bg_sos_button.xml](file:///C:/Users/Msi2/AndroidStudioProjects/VitalHome/app/src/main/res/drawable/bg_sos_button.xml)
Añadiremos un borde (stroke) blanco semitransparente para que el botón resalte mejor sobre el fondo oscuro y se vea más profesional.

#### [MODIFY] [activity_home.xml](file:///C:/Users/Msi2/AndroidStudioProjects/VitalHome/app/src/main/res/layout/activity_home.xml)
*   Agregaremos `app:backgroundTint="@null"` al botón SOS para asegurar que use el color rojo definido en el drawable y no el color por defecto del tema.
*   Aumentaremos la elevación (`elevation`) para darle profundidad.
*   Corregiremos el `textColorLink` que tiene un color casi invisible.

### [Configuración]

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/Msi2/AndroidStudioProjects/VitalHome/app/src/main/AndroidManifest.xml)
Eliminaremos la categoría `LAUNCHER` de `AlertActivity` para evitar que aparezcan dos iconos de la misma app en el dispositivo.

## Verification Plan

### Manual Verification
1.  **Ejecutar la aplicación**: Verificar que solo aparezca un icono en el menú.
2.  **Pantalla de Inicio**: Comprobar que el botón SOS sea de color rojo brillante y tenga un borde sutil.
3.  **Interacción**: Tocar el botón y verificar que el efecto "ripple" (onda) se vea correctamente.
