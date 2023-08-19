abstract class Parser<T> {
    abstract fun parse(input: String): ParserResult<T>

    fun <R> then(right: Parser<R>): Then<T, R> {
        return Then(this, right)
    }

    fun padded(): Padded<T> {
        return Padded(this)
    }
}

abstract class ParseError {
    abstract override fun toString(): String
    fun<T> toResult(remaining: String): ParserResult<T> {
        return ParserResult(remaining, null, listOf(this))
    }
}

class JustError(private val expected: String, private val got: String): ParseError() {
    override fun toString(): String {
        return "Expected: \"$expected\" got: \"$got\""
    }
}

data class ParserResult<T>(val remaining: String, val result: T?, val errors: List<ParseError> = listOf())

class Just(private val just: String) : Parser<String>() {
    override fun parse(input: String): ParserResult<String> {
        if (input.startsWith(just)) {
            return ParserResult(just, input.substring(just.length..<input.length))
        } else {
            val diff = input.substring(just.indices)
            return JustError(just, diff).toResult(input)
        }
    }
}

class Then<L, R>(private val left: Parser<L>, private val right: Parser<R>) : Parser<Pair<L, R>>() {
    override fun parse(input: String): ParserResult<Pair<L, R>> {
        val leftResult = left.parse(input)
        if(leftResult.errors.isNotEmpty()) return ParserResult(input, null, leftResult.errors)

        val rightResult = right.parse(leftResult.remaining)
        if(rightResult.errors.isNotEmpty()) return ParserResult(input, null, rightResult.errors)

        return ParserResult(rightResult.remaining, Pair(leftResult.result as L, rightResult.result as R))
    }
}

class Padded<T>(private val parser: Parser<T>) : Parser<T>() {
    override fun parse(input: String): ParserResult<T> {
        val trimmedStart = input.trimStart()
        val result = parser.parse(trimmedStart)
        val trimmedEnd = result.remaining.trimStart()

        return ParserResult(trimmedEnd, result.result)
    }
}

fun main() {
    val text = "Hello,   world!  "
    val result = Just("Hello, ").then(Just("world!").padded()).parse(text)
    println("Result: $result")
}