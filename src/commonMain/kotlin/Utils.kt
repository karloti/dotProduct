import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.time.measureTimedValue

expect fun p(s: String)

fun dotTreadOne(a: DoubleArray, b: DoubleArray): Double {
    var sum = 0.0
    for (i in a.indices) sum += a[i] * b[i]
    return sum
}

suspend fun dotMultiTread(a: DoubleArray, b: DoubleArray, stepBy: Int = 10_000_000): Double = coroutineScope {
    a.indices.step(stepBy)
        .asSequence()
        .plus(a.size)
        .runningFold(0..0) { a, b -> (a.last + 1)..<b }
        .drop(2) // All before is to make a sequence of scopes = [0..stepBy], [stepBy+1..StepBy*2], ...
        .asFlow()
        .map { indices -> async { indices.sumOf { i -> a[i] * b[i] } } }
        .buffer()
        .map(Deferred<Double>::await)
        .fold(0.0, Double::plus)
}

suspend fun positionalEmbedding(dimension: Int): Flow<List<Double>> = flow {
    var pos = 1
    while (true) {
        val embedding = (0..<dimension step 2)
            .asFlow()
            .buffer()
            .transform { dimIndex ->
                val d = pos / (10000.0).pow(dimIndex / dimension)
                emit(sin(d))
                emit(cos(d))
            }
            .take(dimension)
            .toList()
        emit(embedding)
        pos++
    }
}

suspend fun start(size: Int = 1_000_000_000): Unit = coroutineScope {
    p("Generating Vec1 with $size dimensions")
    val vec1 = DoubleArray(size) { it.toDouble() }
    p((vec1.joinToString(separator = ", ", prefix = "Vec1 = [", postfix = "]", limit = 10) { "$it".take(4) } + "\n"))

    p("Generating Vec2 with $size dimensions")
    val vec2 = DoubleArray(size) { it.toDouble() }
    p((vec1.joinToString(separator = ", ", prefix = "Vec2 = [", postfix = "]", limit = 10) { "$it".take(4) } + "\n"))
    p("Star Dot product [single thread with linear iteration *for cycle ] ...")
    measureTimedValue { dotTreadOne(vec1, vec2) }.also { (value, duration) ->
        p("Dot product Vec1 * Vec2 = $value")
        p("Calculation time is $duration")
    }

    (1..5).runningFold(1) { a, _ -> a * 10 }.map { treads ->
        val stepBy = vec1.size / treads
        p("\nStar Dot product [threads = $treads by scopes $stepBy of elements]")
        measureTimedValue { dotMultiTread(vec1, vec2, stepBy) }.also { (value, duration) ->
            p("Dot product Vec1 * Vec2 = $value")
            p("Calculation time is $duration")
        }
    }
}