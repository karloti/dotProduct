import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.p

actual fun p(s: String): Unit {
    document.body?.append {
        s.split("\n").forEach {
            p { text(it.trim()) }
        }
    }
}

suspend fun main() = start(size = 1_000_00_000)