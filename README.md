# Custom Health HUD — Fabric 1.20.1

Port del mod `customhealthhud` 1.0 (Forge 1.20.1, autor POH) a Fabric 1.20.1.
Misma funcionalidad: HUD de vida/XP personalizado, comando `/hud`, mismo archivo `config/customhud.json`.

## Compilar el .jar

**Opción A — sin instalar nada (GitHub Actions):**
1. Sube esta carpeta a un repo de GitHub.
2. Pestaña *Actions* → workflow `build` → cuando termine, baja el artifact `customhealthhud-fabric` (ahí está el .jar).

**Opción B — en tu PC:** necesitas JDK 17 y Gradle 8.6+.
```
gradle build
```
El jar queda en `build/libs/customhealthhud-1.0.jar`.

## Instalar
Pon el jar en `mods/` junto con **Fabric Loader ≥ 0.14.21** y **Fabric API** (1.20.1).
Si quieres que el botón Save sincronice a todos los jugadores, el mod tiene que estar también en el servidor.

## Qué cambió respecto a Forge
| Forge | Fabric |
|---|---|
| `RegisterGuiOverlaysEvent` | `HudRenderCallback` |
| `RenderGuiOverlayEvent.Pre` + `setCanceled` | `mixin/GuiMixin` |
| `SimpleChannel` (`customhealthhud:main`) | 2 canales de Fabric API (`hud_config`, `sync_hud_config`) |
| `RegisterClientCommandsEvent` | `ClientCommandRegistrationCallback` |
| `mods.toml` | `fabric.mod.json` |

Pantalla de configuración, config, colores, sliders y lógica de dibujo: sin cambios.
