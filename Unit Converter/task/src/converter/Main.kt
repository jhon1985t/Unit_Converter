package converter

import java.util.*

enum class UnitType {
    LENGTH, WEIGHT, TEMPERATURE
}

enum class Unit (val value: Double, val names: List<String>, val type: UnitType) {
    REF(1.0, listOf("???","???","???"), UnitType.LENGTH),
    METER(1.0, listOf("m", "meter", "meters"), UnitType.LENGTH),
    KILOMETER(1000.0, listOf("km", "kilometer", "kilometers"), UnitType.LENGTH),
    MILLIMETER(0.001, listOf("mm", "millimeter", "millimeters"), UnitType.LENGTH),
    CENTIMETER(0.01, listOf("cm", "centimeter", "centimeters"), UnitType.LENGTH),
    MILE(1609.35, listOf("mi", "mile", "miles"), UnitType.LENGTH),
    YARD(0.9144, listOf("yd", "yard", "yards"), UnitType.LENGTH),
    FOOT(0.3048, listOf("ft", "foot", "feet"), UnitType.LENGTH),
    INCH(0.0254, listOf("in", "inch", "inches"), UnitType.LENGTH),
    GRAM(1.0, listOf("g", "gram", "grams"), UnitType.WEIGHT),
    KILOGRAM(1000.0, listOf("kg", "kilogram", "kilograms"), UnitType.WEIGHT),
    MILLIGRAM(0.001, listOf("mg", "milligram", "milligrams"), UnitType.WEIGHT),
    POUND(453.592, listOf("lb", "pound", "pounds"), UnitType.WEIGHT),
    OUNCE(28.3495, listOf("oz", "ounce", "ounces"), UnitType.WEIGHT),
    CELSIUS(0.0, listOf("c", "dc", "celsius", "degree Celsius", "degrees Celsius"), UnitType.TEMPERATURE),
    FAHRENHEIT(0.0, listOf("f", "df", "fahrenheit", "degree Fahrenheit", "degrees Fahrenheit"), UnitType.TEMPERATURE),
    KELVIN(0.0, listOf("k", "kelvin", "kelvins"), UnitType.TEMPERATURE);

    companion object {
        fun findUnit(name: String): Unit? {
            val lowercaseName = name.lowercase()
            return values().firstOrNull { unit ->
                unit.names.any { it.lowercase() == lowercaseName }
            }
        }
    }
}

fun main() {
    while (true) {
        println("Enter what you want to convert (or exit):")
        val input = readln().trim()

        if (input.equals("exit", ignoreCase = true)) break

        // Verificar si "convertTo" está presente en el input.
        if (input.contains("convertTo", ignoreCase = true)) {
            println("Conversion from ??? to ??? is impossible")
            continue
        }

        // Se busca el índice de "to" considerando que puede ser parte de una unidad compuesta.
        val parts = input.split(" ")
        val indexOfSeparator = parts.indexOfFirst { it == "to" || it == "in" }
        if (indexOfSeparator == -1 || indexOfSeparator < 2 || indexOfSeparator >= parts.size - 1) {
            println("Parse error")
            continue
        }

        try {
            // Extracción y conversión de la cantidad.
            val number = parts[0].toDouble()
            // La unidad de origen se forma juntando las partes desde el índice 1 hasta el índice de "to".
            val fromUnitInput = parts.subList(1, indexOfSeparator).joinToString(" ")
            // La unidad de destino se forma desde el índice después de "to" hasta el final.
            val toUnitInput = parts.subList(indexOfSeparator + 1, parts.size).joinToString(" ")

            val fromUnit = Unit.findUnit(fromUnitInput)
            val toUnit = Unit.findUnit(toUnitInput)

            // En la parte donde se imprime el mensaje de error para conversiones imposibles:
            if (fromUnit == null || toUnit == null || fromUnit.type != toUnit.type) {
                val fromUnitName = fromUnit?.let { getUnitName(it, 2.0) } ?: "???" // Usar 2.0 para asegurar plural
                val toUnitName = toUnit?.let { getUnitName(it, 2.0) } ?: "???" // Usar 2.0 para asegurar plural
                println("Conversion from $fromUnitName to $toUnitName is impossible")
                continue
            }

            // Validación de números negativos solo para peso y longitud
            if (number < 0 && (fromUnit.type == UnitType.WEIGHT || fromUnit.type == UnitType.LENGTH)) {
                println("${fromUnit.type.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} shouldn't be negative.")
                continue
            }

            val convertedValue = if (fromUnit.type == UnitType.TEMPERATURE) {
                convertTemperature(number, fromUnit, toUnit)
            } else {
                number * fromUnit.value / toUnit.value
            }

            println("$number ${getUnitName(fromUnit, number)} is $convertedValue ${getUnitName(toUnit, convertedValue)}")
        } catch (e: Exception) {
            println("Parse error")
        }
    }
}

fun convertTemperature(value: Double, from: Unit, to: Unit): Double {
    return when {
        from == Unit.CELSIUS && to == Unit.FAHRENHEIT -> value * 9 / 5 + 32
        from == Unit.FAHRENHEIT && to == Unit.CELSIUS -> (value - 32) * 5 / 9
        from == Unit.CELSIUS && to == Unit.KELVIN -> value + 273.15
        from == Unit.KELVIN && to == Unit.CELSIUS -> value - 273.15
        from == Unit.FAHRENHEIT && to == Unit.KELVIN -> (value + 459.67) * 5 / 9
        from == Unit.KELVIN && to == Unit.FAHRENHEIT -> value * 9 / 5 - 459.67
        else -> value
    }
}

fun getUnitName(unit: Unit, value: Double): String {
    return when (unit) {
        Unit.KELVIN -> if(value == 1.0) "kelvin" else "kelvins" // 'kelvin' es correcto tanto en singular como en plural.
        else -> {
            when (unit.type) {
                UnitType.TEMPERATURE -> {
                    // Para temperaturas, maneja casos singulares y plurales específicos.
                    when {
                        unit == Unit.CELSIUS && value == 1.0 -> "degree Celsius"
                        unit == Unit.CELSIUS -> "degrees Celsius"
                        unit == Unit.FAHRENHEIT && value == 1.0 -> "degree Fahrenheit"
                        unit == Unit.FAHRENHEIT -> "degrees Fahrenheit"
                        else -> unit.names.first() // Utiliza el primer nombre como predeterminado.
                    }
                }
                else -> {
                    // Para longitud y peso, decide entre singular y plural basado en el valor.
                    if (value == 1.0) unit.names.getOrNull(1) ?: unit.names.first() // Singular
                    else unit.names.getOrNull(2) ?: unit.names.first() // Plural
                }
            }
        }
    }
}
