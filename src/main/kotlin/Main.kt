abstract class Parser<T> {
    abstract fun parse(input: String): ParserResult<T>

    fun <R> then(right: Parser<R>): Then<T, R> {
        return Then(this, right)
    }

    fun padded(): Padded<T> {
        return Padded(this)
    }
}

class ParseError(parser: Class<Any>, message: String) : Exception("($parser) $message")

data class ParserResult<T>(val result: T, val remaining: String)

class Just(private val just: String) : Parser<String>() {
    override fun parse(input: String): ParserResult<String> {
        if (input.startsWith(just)) {
            return ParserResult(just, input.substring(just.length..<input.length))
        } else {
            val diff = input.substring(just.indices)
            throw ParseError(Just::class.java as Class<Any>, "Couldn't parse: \"$just\", got: \"$diff\"!")
        }
    }
}

class Then<L, R>(private val left: Parser<L>, private val right: Parser<R>) : Parser<Pair<L, R>>() {
    override fun parse(input: String): ParserResult<Pair<L, R>> {
        val leftResult = left.parse(input)
        val rightResult = right.parse(leftResult.remaining)

        return ParserResult(Pair(leftResult.result, rightResult.result), rightResult.remaining)
    }
}

class Padded<T>(private val parser: Parser<T>) : Parser<T>() {
    override fun parse(input: String): ParserResult<T> {
        val trimmedStart = input.trimStart()
        val result = parser.parse(trimmedStart)
        val trimmedEnd = result.remaining.trimStart()

        return ParserResult(result.result, trimmedEnd)
    }
}

fun main() {
    val text = "Hello,   world!  "
    val result = Just("Hello, ").then(Just("world!").padded()).parse(text)
    println("Result: $result")
}