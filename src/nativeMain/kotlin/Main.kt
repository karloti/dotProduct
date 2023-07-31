import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

actual fun p(s: String) = println(s)

fun main() = runBlocking(Dispatchers.Default) { start() }