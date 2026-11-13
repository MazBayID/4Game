import express from 'express';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { createServer as createViteServer } from 'vite';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const PORT = Number(process.env.PORT || 3000);
const IS_PROD = process.env.NODE_ENV === 'production';

async function startServer() {
  const app = express();
  app.use(express.json());

  // Serve local Pyodide 3.14 WASM runtime offline-first with zero external CDN reliance
  const pyodideDir = path.resolve(__dirname, 'node_modules/pyodide');
  if (fs.existsSync(pyodideDir)) {
    app.use('/pyodide', express.static(pyodideDir));
  }

  app.get('/api/health', (_req, res) => {
    res.json({ status: 'ok', app: 'neo-null-python-protocol', pythonRuntime: 'pyodide-3.14' });
  });

  // Serve Android PNG drawables directly for preview & download in the Android Source Drawer
  const androidDrawableDir = path.resolve(__dirname, 'app/src/main/res/drawable');
  if (fs.existsSync(androidDrawableDir)) {
    app.use('/android-drawables', express.static(androidDrawableDir));
  }

  // Expose generated Android Kotlin source files and PNG drawables for inspection/download
  app.get('/api/android-sources', (_req, res) => {
    const allowedRoots = [
      'settings.gradle.kts',
      'build.gradle.kts',
      'gradle.properties',
      'app/build.gradle.kts',
      'app/src/main/AndroidManifest.xml',
      'app/src/main/java/com/neonull/protocol/MainActivity.kt',
      'app/src/main/java/com/neonull/protocol/audio/CyberAudioEngine.kt',
      'app/src/main/java/com/neonull/protocol/localization/LanguageManager.kt',
      'app/src/main/java/com/neonull/protocol/ui/theme/Color.kt',
      'app/src/main/java/com/neonull/protocol/ui/theme/Theme.kt',
      'app/src/main/java/com/neonull/protocol/syntax/SyntaxHighlighter.kt',
      'app/src/main/java/com/neonull/protocol/data/CurriculumData.kt',
      'app/src/main/java/com/neonull/protocol/viewmodel/LearningViewModel.kt',
      'app/src/main/java/com/neonull/protocol/ui/components/CyberHudComponents.kt',
      'app/src/main/java/com/neonull/protocol/ui/screens/OnboardingScreen.kt',
      'app/src/main/java/com/neonull/protocol/ui/screens/StoryHubScreen.kt',
      'app/src/main/java/com/neonull/protocol/ui/screens/TheoryViewerScreen.kt',
      'app/src/main/java/com/neonull/protocol/ui/screens/TerminalLearningScreen.kt',
      'app/src/main/java/com/neonull/protocol/ui/screens/CipherCodexDialog.kt',
    ];

    const files = allowedRoots
      .map((relPath) => {
        const absPath = path.resolve(__dirname, relPath);
        if (!fs.existsSync(absPath)) return null;
        return {
          path: relPath,
          name: path.basename(relPath),
          content: fs.readFileSync(absPath, 'utf-8'),
        };
      })
      .filter(Boolean);

    const pngAssets = fs.existsSync(androidDrawableDir)
      ? fs
          .readdirSync(androidDrawableDir)
          .filter((name) => name.endsWith('.png'))
          .sort()
          .map((name) => {
            const absPath = path.join(androidDrawableDir, name);
            const stat = fs.statSync(absPath);
            return {
              name,
              path: `app/src/main/res/drawable/${name}`,
              url: `/android-drawables/${name}`,
              sizeKb: Math.round(stat.size / 1024),
            };
          })
      : [];

    res.json({ files, pngAssets });
  });

  if (IS_PROD) {
    const distPath = path.resolve(__dirname, 'dist');
    app.use(express.static(distPath));
    app.get('*', (_req, res) => {
      res.sendFile(path.join(distPath, 'index.html'));
    });
  } else {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`NEO-NULL server running on http://0.0.0.0:${PORT}`);
  });
}

startServer().catch((err) => {
  console.error('Failed to start server:', err);
  process.exit(1);
});
