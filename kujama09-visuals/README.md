# kujama09 visuals

Mod de Fabric (solo cliente) para **Minecraft 1.21.11**, independiente de `kujama09` (intro y menú).
Pensado para un look limpio y oscuro: cielo negro con líneas, partículas claras, HUD minimalista y la música
de Spotify en pantalla.

> **Estado:** escrito sin poder compilarlo localmente. Se compila con GitHub Actions (ver `main.yml`) o con
> `./gradlew build` (necesita JDK 25). El JAR queda en `build/libs/kujama09-visuals-1.0.0.jar` (no uses el `-sources`).
> Ponelo en la carpeta `mods`, junto a Fabric API.

## Qué incluye

- **Cielo custom (`/kv sky void`, por defecto):** domo negro + líneas blancas animadas que siguen la cámara.
  Minecraft no deja reemplazar el cielo del Overworld con una textura, así que las líneas son una capa 2D con
  parallax, recortada en el horizonte. Funciona mejor en terreno abierto. Solo en el Overworld y con cielo visible.
- **Partículas custom:** destellos y hojas pálidas flotando alrededor tuyo (solo visual, no afectan al servidor).
- **HUD limpio:** FPS arriba al centro, hora, coordenadas, teclas W A S D + LMB/RMB y CPS, sin paneles pesados.
- **Música de Spotify (solo Windows):** muestra título y artista en una tarjeta arriba. Lee el título de la ventana
  de la app de escritorio de Spotify con PowerShell cada 3 s; todo es local, no usa internet ni tu cuenta. No muestra
  portada ni progreso (esos datos no están en el título de la ventana). Solo aparece mientras suena una canción.
- **Quitar lluvia:** `/kv rain off`, solo visual.
- Otros cielos de color: `red`, `aurora`, `purple`, `fire`.

## Comandos (`/kv`)

| Comando | Qué hace |
|---|---|
| `/kv hud` `bar` `keys` `xyz` `cps` | activa o desactiva el HUD completo o cada parte |
| `/kv music` | muestra u oculta la tarjeta de Spotify |
| `/kv particles` | activa o desactiva las partículas |
| `/kv lines` | activa o desactiva las líneas del cielo |
| `/kv sky void\|off\|red\|aurora\|purple\|fire` | elige el cielo (`off` = cielo normal) |
| `/kv sky strength 0-100` | intensidad del cielo |
| `/kv scale 1-3` | tamaño del HUD |
| `/kv rain off\|on` | quita o devuelve la lluvia (solo visual) |
| `/kv reset` | restaura los valores |

Las opciones se guardan en `config/kujama09_visuals.json`. Si ya existía ese archivo de una versión anterior,
usá `/kv reset` para tomar los valores nuevos.

## Lo que NO incluye

Alas, modelo de jugador translúcido, anillos en el suelo, el efecto de dinero, TNT custom y las funciones de
combate del cliente del video.

## Novedades 1.1.0
- **Menu** con Right Shift (o `/kv menu`): pestanas Visual, World, Themes y Settings.
- **Temas de color** (16): `/kv theme <nombre>` o desde la pestana Themes.
- **Fullbright**: `/kv fullbright` o en World (nivel 2-15). Algunos servidores lo prohiben.
- **HUD nuevo**: barra superior, panel XYZ, Modules y KeyBinds (`/kv modules`, `/kv keybinds`).

## Novedades 1.2.0
- **Manos negras** en primera persona con borde del color del tema (`/kv hands`).
- **Logo en el cielo** (`/kv logo`, tamano en el menu). Por defecto es el zorro de kujama09. Para usar tu propia imagen, guardala como `config/kujama09_visuals/logo.png` y ejecuta `/kv logo reload`.

## Novedades 1.3.0
- **Cielos realistas** (`/kv sky storm|overcast|dusk`): tormenta, nublado y atardecer. Conservan el ciclo dia/noche.
- **Truenos en las nubes** (`/kv thunder`): destellos y ramas dentro de las nubes, sin rayos que caigan al suelo. `/kv thunderrate 0-100` y `/kv thundersound` para frecuencia y sonido. Todo desde la pestana Sky del menu.
- 1.3.1: antes de cada trueno, las nubes se iluminan de a poco con parpadeos irregulares (carga previa) y despues vienen los destellos.

## Novedades 1.4.0
- **Portada del album** en la tarjeta de Spotify (`/kv cover`). Se pide al control de medios de Windows solo cuando cambia la cancion; todo local. Si Windows no la entrega, queda el icono de nota.
