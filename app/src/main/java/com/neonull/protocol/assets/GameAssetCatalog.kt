package com.neonull.protocol.assets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import org.json.JSONObject
import java.io.IOException

enum class GameAssetCategory { ICON, CHARACTER, BACKGROUND, SPRITESHEET, EFFECT }

data class GameAssetSpec(
    val key: String,
    val category: GameAssetCategory,
    val path: String,
    val width: Int,
    val height: Int,
    val columns: Int = 1,
    val rows: Int = 1,
    val frameCount: Int = 1,
    val framesPerSecond: Int = 1,
    val loop: Boolean = false
) {
    val frameWidth: Int get() = width / columns
    val frameHeight: Int get() = height / rows
}

class GameAssetCatalog private constructor(
    private val context: Context,
    specs: List<GameAssetSpec>
) {
    private val byKey = specs.associateBy { it.key }
    private val bitmapCache = object : LruCache<String, Bitmap>(CACHE_KILOBYTES) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    val all: List<GameAssetSpec> = specs.toList()

    fun find(key: String): GameAssetSpec? = byKey[key]

    /** Returns null when an optional/planned asset has not been supplied yet. */
    fun loadBitmap(key: String): Bitmap? {
        val spec = byKey[key] ?: return null
        bitmapCache.get(key)?.let { return it }
        val decoded = try {
            context.assets.open(spec.path).use { BitmapFactory.decodeStream(it) }
        } catch (_: IOException) {
            null
        } ?: return null

        if (decoded.width != spec.width || decoded.height != spec.height) {
            decoded.recycle()
            return null
        }
        bitmapCache.put(key, decoded)
        return decoded
    }

    /** Crops one sprite frame; frame indexes are zero-based, row-major. */
    fun loadFrame(key: String, frameIndex: Int): Bitmap? {
        val spec = byKey[key] ?: return null
        if (spec.category != GameAssetCategory.SPRITESHEET &&
            spec.category != GameAssetCategory.EFFECT
        ) return null
        if (frameIndex !in 0 until spec.frameCount) return null
        val sheet = loadBitmap(key) ?: return null
        val column = frameIndex % spec.columns
        val row = frameIndex / spec.columns
        return Bitmap.createBitmap(
            sheet,
            column * spec.frameWidth,
            row * spec.frameHeight,
            spec.frameWidth,
            spec.frameHeight
        )
    }

    companion object {
        private const val MANIFEST_PATH = "byteghost/asset_manifest.json"
        private const val CACHE_KILOBYTES = 8 * 1024

        fun load(context: Context): GameAssetCatalog {
            val json = context.assets.open(MANIFEST_PATH).bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            require(root.getInt("schemaVersion") == 1) { "Unsupported ByteGhost asset manifest version" }
            val entries = root.getJSONArray("assets")
            val specs = ArrayList<GameAssetSpec>(entries.length())
            val keys = HashSet<String>()

            for (index in 0 until entries.length()) {
                val item = entries.getJSONObject(index)
                val key = item.getString("key")
                val path = item.getString("path")
                require(keys.add(key)) { "Duplicate ByteGhost asset key: $key" }
                require(path.startsWith("byteghost/") && !path.contains("..") && !path.startsWith("/")) {
                    "Unsafe ByteGhost asset path: $path"
                }
                val category = GameAssetCategory.valueOf(item.getString("category").uppercase())
                val spec = GameAssetSpec(
                    key = key,
                    category = category,
                    path = path,
                    width = item.getInt("width"),
                    height = item.getInt("height"),
                    columns = item.optInt("columns", 1),
                    rows = item.optInt("rows", 1),
                    frameCount = item.optInt("frameCount", 1),
                    framesPerSecond = item.optInt("fps", 1),
                    loop = item.optBoolean("loop", false)
                )
                require(spec.width > 0 && spec.height > 0) { "Invalid dimensions for $key" }
                require(spec.columns > 0 && spec.rows > 0) { "Invalid sprite grid for $key" }
                require(spec.width % spec.columns == 0 && spec.height % spec.rows == 0) {
                    "Sprite sheet dimensions must divide evenly for $key"
                }
                require(spec.frameCount in 1..(spec.columns * spec.rows)) {
                    "Invalid frame count for $key"
                }
                require(spec.framesPerSecond > 0) { "Invalid frame rate for $key" }
                specs += spec
            }
            return GameAssetCatalog(context.applicationContext, specs)
        }
    }
}
