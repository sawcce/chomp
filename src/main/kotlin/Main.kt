abstract class Parser<T> {
    abstract fun parse(input: String): ParserResult<T>

    fun <R> then(right: Parser<R>): Then<T, R> {
        return Then(this, right)
    }
}

class ParseError(private val parser: Class<Any>, private val _message: String) : Exception("($parser) $_message")

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

fun main(args: Array<String>) {
    val text = "Hello,, world!"
    val result = Just("Hello, ").then(Just("world!")).parse(text)
    println("Result: $result")
}