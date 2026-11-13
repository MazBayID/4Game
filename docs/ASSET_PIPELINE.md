# ByteGhost asset pipeline

The game asset pipeline is intentionally separate from Android's `res/drawable` resource IDs. New artwork belongs in **`app/src/main/assets/byteghost/`**, where nested folders are supported.

## Folder layout

```
app/src/main/assets/byteghost/
├── asset_manifest.json
├── icons/          # transparent PNG icons, normally 64×64
├── characters/     # character portraits, normally 512×512
├── sprites/        # character sprite sheets
├── effects/        # VFX sprite sheets
└── backgrounds/    # 1280×720 scene backgrounds
```

Existing legacy images under `res/drawable/` remain untouched and continue to be addressed by their current resource IDs.

## Manifest is the source of truth

Each asset has a stable key, category, relative path, expected sheet dimensions, and optional animation metadata. Add the file at the declared path to activate a planned entry. The catalog safely returns `null` for missing optional files rather than crashing a screen.

Use `GameAssetCatalog.load(context)` once at the application/game layer and retain the instance. Use `find(key)` to inspect metadata, `loadBitmap(key)` for a still image or complete sheet, and `loadFrame(key, frameIndex)` for a zero-based sprite frame.

## Sprite sheet contract

- PNG with equal-sized cells, no gutters, margins, borders, labels, or frame numbers.
- Frames are read left-to-right, then top-to-bottom.
- Sheet width must divide evenly by columns; sheet height must divide evenly by rows.
- The manifest frame count may be lower than the number of grid cells, but never higher.
- Keep a character's scale, silhouette, anchor point, and lighting consistent across frames.
- Transparent background for characters, icons, and effects. Background scenes may be opaque.
- Do not place text inside icons; UI labels remain localizable Compose text.

## Initial target sizes

| Asset family | Target |
|---|---:|
| Standard icons | 64×64 px |
| Large boss/status icon | 96×96 px |
| Character portrait | 512×512 px |
| GLITCH idle/talking, 4×4 | 1024×1024 px (256×256 per frame) |
| 8-frame character reaction, 4×2 | 1024×512 px (256×256 per frame) |
| 4×4 effects | 1024×1024 px |
| 4×2 effects | 1024×512 px |
| Scene backgrounds | 1280×720 px |

The manifest currently includes planned keys for navigation, progression, Python concepts, GLITCH/Overlord, effects, and environments. These are specifications, not claims that the artwork already exists.

## Integration rules

1. Preserve the existing ByteGhost visual identity and frozen screen layout.
2. When an asset pack arrives, audit actual filenames, image dimensions, alpha channel, and sprite grid before adding files.
3. Update the manifest to match verified files; never rename a supplied file silently.
4. Map each key to its intended screen/component in an asset integration sheet before wiring it in.
5. Prefer Compose `Image` for static assets. For animation, drive `loadFrame` from a lifecycle-aware frame clock; do not create one coroutine/timer per frame per item.
6. Keep image decoding off the main thread before production use. The current catalog is a lightweight foundation and its synchronous decode methods should be called from an IO dispatcher.
7. Add tests for manifest validity, missing-file behavior, dimensions, and sprite-frame bounds whenever assets are imported.
