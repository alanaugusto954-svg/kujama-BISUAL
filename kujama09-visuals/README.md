# kujama09 visuals

Mod de Fabric (solo cliente) para **Minecraft 1.21.11**. Es independiente de `kujama09` (intro y menú):
se pueden usar los dos juntos o por separado.

> **Estado:** escrito sin poder compilarlo (el entorno de creación no llega a `maven.fabricmc.net`).
> Compilalo con GitHub Actions (el repo ya trae `.github/workflows/build.yml`) o con `./gradlew build`.
> El JAR queda en `build/libs/kujama09-visuals-1.0.0.jar`. Ponelo en la carpeta `mods`, junto a Fabric API.

## Qué incluye

- **HUD**: barra superior (nombre, FPS, hora), teclas W A S D y SPACE que se resaltan al apretarlas,
  coordenadas XYZ y contador de CPS (clic izquierdo | derecho).
- **Cielo de colores**: rojo, aurora, púrpura y fuego. Tiñe la niebla/horizonte (mixin opcional) y agrega un
  degradado de color en la parte alta de la pantalla. El domo del cielo de Minecraft no se reemplaza, así que el
  efecto es una atmósfera de color, no un cielo nuevo.

## Controles (comando `/kv` dentro del juego)

| Comando | Qué hace |
|---|---|
| `/kv hud` `/kv bar` `/kv keys` `/kv xyz` `/kv cps` | activa o desactiva el HUD completo o cada parte |
| `/kv scale 1-3` | tamaño del HUD |
| `/kv sky off\|red\|aurora\|purple\|fire` | elige el cielo |
| `/kv sky strength 0-100` | intensidad del cielo de color |
| `/kv reset` | restaura los valores |

Las opciones se guardan en `config/kujama09_visuals.json`.

## Lo que NO incluye

Alas, modelo de jugador translúcido, anillos en el suelo y las funciones de combate del cliente del video.
