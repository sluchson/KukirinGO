package pl.kukiringo.app

import android.app.Application
import org.osmdroid.config.Configuration
import pl.kukiringo.app.data.repository.AppContainer
import java.io.File

class KukirinApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
        copyDemoClips()

        // OpenStreetMap: identyfikacja aplikacji i cache kafelków w pamięci aplikacji.
        Configuration.getInstance().apply {
            load(this@KukirinApplication, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = packageName
            osmdroidBasePath = File(cacheDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }
    }

    /**
     * Klipy demo z `assets/demo_clips/` (jeśli są) trafiają raz do kukiTok.
     * Folder jest w .gitignore — klipy żyją tylko w lokalnej wersji do pokazu.
     */
    private fun copyDemoClips() {
        val clipsDir = File(filesDir, "clips").apply { mkdirs() }
        val names = try {
            assets.list("demo_clips")?.filter { it.endsWith(".mp4") }.orEmpty().sorted()
        } catch (e: Exception) {
            emptyList()
        }
        names.forEachIndexed { index, name ->
            val target = File(clipsDir, name)
            if (target.exists() && target.length() > 0) return@forEachIndexed
            try {
                assets.open("demo_clips/$name").use { input -> target.outputStream().use { input.copyTo(it) } }
                // starsze klipy demo niżej w feedzie
                target.setLastModified(System.currentTimeMillis() - (index + 1) * 60_000L)
            } catch (e: Exception) {
                target.delete()
            }
        }
    }
}
