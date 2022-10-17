import com.rafael.roman.RNumber
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

internal class RNumberTest {

    class OperandsProvider: ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(Arguments.of("IX", "X", "XIX"),
            Arguments.of("IX", "VI", "XV"),
            Arguments.of("VIII", "VIII", "XVI"),
                Arguments.of("IX", "XX", "XXIX"),
                Arguments.of("IX", "IX", "XVIII"),
                Arguments.of("XV", "V", "XX"))
        }

    }

    @ParameterizedTest(name = "Test: {0} + {1} = {2}")
    @ArgumentsSource(OperandsProvider::class)
    fun testAdd(addend1: String, addend2: String, result: String) {
        val res = RNumber.parse(addend1) + RNumber.parse(addend2);
        assertEquals(result, res.toString())
    }
}