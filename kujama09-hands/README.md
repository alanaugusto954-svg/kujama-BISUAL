# kujama09 hands

Mod visual para **Minecraft Java 1.21.11** (Fabric). Cambia solo como se dibujan la mano y el objeto en primera persona.

- **Manos alejadas:** se mueven hacia las esquinas inferiores y se reducen, dejando el centro libre. No toca la camara ni la posicion real de nada.
- **Resplandor dorado:** halo en las esquinas donde estan las manos. Es un efecto de pantalla aproximado, no un contorno exacto de la silueta.
- Todo es configurable: tamano, desplazamiento horizontal, vertical y profundidad, color e intensidad.

## Comandos
`/kh menu` (abre el panel con sliders), `/kh far`, `/kh glow`, `/kh scale 30-150`, `/kh x|y|z -100..100`, `/kh intensity 0-100`, `/kh color #RRGGBB`, `/kh reset`.

## Instalar
Copia el jar (el que no termina en `-sources`) a la carpeta `mods`. Requiere Fabric Loader 0.19+ y Fabric API para 1.21.11.

## Compilar
Con GitHub Actions: sube esta carpeta al repo y ejecuta el flujo "Build kujama09-hands". En local hace falta JDK 25: `./gradlew build` y el jar queda en `build/libs/`.
