# ByteGhost asset pipeline

The asset pipeline is separate from Android `res/drawable` resource IDs. Game artwork belongs in **`app/src/main/assets/byteghost/`**, where nested folders are supported.

## Current pack

The repository currently contains the imported ByteGhost pack: backgrounds, character portraits, effects, icons, sprite sheets, and `asset_manifest.json`. All file paths declared by the current manifest were present during the repository audit.

A manifest entry confirms that an asset exists in the pack; it does not mean that the asset is already used by gameplay. Only a subset is currently referenced by the inspected application UI. Other assets remain intentionally deferred until their gameplay purpose exists. Do not force them into unrelated screens just to increase the integration count.

## Folder layout

```
app/src/main/assets/byteghost/
├── asset_manifest.json
├── icons/
├── characters/
├── sprites/
├── effects/
└── backgrounds/
```

Existing legacy images under `res/drawable/` remain untouched and continue to be addressed by their current resource IDs.

## Manifest contract

Each asset has a stable key, category, relative path, expected sheet dimensions, and optional animation metadata. Use `GameAssetCatalog.load(context)` once at the app/game layer and retain the instance. Use `find(key)` to inspect metadata, `loadBitmap(key)` for a still image or complete sheet, and `loadFrame(key, frameIndex)` for a zero-based sprite frame.

The catalog may return `null` for a missing or invalid optional asset; callers must provide a sensible fallback.

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

## Integration rules

1. Preserve the existing ByteGhost visual identity and frozen screen layout.
2. Before importing new artwork, audit filenames, dimensions, alpha channel, and sprite grid.
3. Update the manifest to match verified files; never rename a supplied file silently.
4. Map each key to an intended gameplay screen/component before wiring it in.
5. Prefer Compose `Image` for static assets. For animation, use a lifecycle-aware frame clock; do not create one coroutine/timer per frame per item.
6. Keep image decoding off the main thread.
7. Add tests for manifest validity, missing-file behavior, dimensions, and sprite-frame bounds when the asset pack changes.

## Compose rendering

Use `GameAssetSprite(catalog, assetKey, modifier, playing)` for a manifest-backed image or sprite animation. It decodes the cached bitmap on an IO dispatcher, crops frames using the manifest grid, and advances according to the declared FPS and loop settings. If an optional asset is unavailable or invalid, the composable displays the supplied fallback content.

Keep one `GameAssetCatalog` instance at the app/game layer and share it; do not parse the manifest in each composable.
