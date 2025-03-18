import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

enum class EnvValueType {
    STRING,
    ARRAY
}

sealed class EnvValue {
    data class StringValue(val value: String) : EnvValue()
    data class ArrayValue(val values: List<String>) : EnvValue()
}

enum class EnvKey(val type: EnvValueType) {
    GEMINI_API_KEYS(EnvValueType.ARRAY),
    OLLAMA_API_KEY(EnvValueType.STRING);

    companion object {
        fun fromString(key: String): EnvKey? = entries.find { it.name == key }
    }
}

object Env {
    private val values: Map<EnvKey, EnvValue> = loadEnv()

    fun get(key: EnvKey): String {
        require(key.type == EnvValueType.STRING) { "ENV: Key $key is not a string type" }
        return when (val value = values[key] ?: throw MissingEnvKeyException(key)) {
            is EnvValue.StringValue -> value.value
            is EnvValue.ArrayValue -> throw InvalidEnvValueTypeException(key, "array value")
        }
    }

    fun getList(key: EnvKey): List<String> {
        require(key.type == EnvValueType.ARRAY) { "ENV: Key $key is not an array type" }
        return when (val value = values[key] ?: throw MissingEnvKeyException(key)) {
            is EnvValue.ArrayValue -> value.values
            is EnvValue.StringValue -> throw InvalidEnvValueTypeException(key, value.value)
        }
    }

    private fun loadEnv(): Map<EnvKey, EnvValue> {
        val envFile = File(".env")
        if (!envFile.exists()) {
            throw EnvFileNotFoundException()
        }

        val result = mutableMapOf<EnvKey, EnvValue>()

        Files.readAllLines(Paths.get(".env")).forEach { line ->
            if (line.isBlank() || line.startsWith("#")) return@forEach

            val parts = line.split("=", limit = 2)
            if (parts.size != 2) {
                println("ENV: Invalid line format: $line")
                return@forEach
            }

            val (key, value) = parts.map { it.trim() }
            val envKey = EnvKey.fromString(key)

            when {
                envKey == null -> println("ENV: Unknown key: $key")
                result.containsKey(envKey) -> println("ENV: Duplicate key: $key")
                else -> {
                    val parsedValue = parseValue(value)
                    if (!validateValueType(envKey, parsedValue)) {
                        throw InvalidEnvValueTypeException(envKey, value)
                    }
                    result[envKey] = parsedValue
                }
            }
        }

        val missingKeys = EnvKey.entries - result.keys
        if (missingKeys.isNotEmpty()) {
            throw MissingEnvKeysException(missingKeys)
        }

        return result.toMap()
    }

    private fun validateValueType(key: EnvKey, value: EnvValue): Boolean {
        return when (key.type) {
            EnvValueType.STRING -> value is EnvValue.StringValue
            EnvValueType.ARRAY -> value is EnvValue.ArrayValue
        }
    }

    private fun parseValue(value: String): EnvValue {
        // If the value starts with [ and ends with ], treat it as an array
        if (value.startsWith("[") && value.endsWith("]")) {
            val arrayValues = value.substring(1, value.length - 1)
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            return EnvValue.ArrayValue(arrayValues)
        }
        // Otherwise, treat it as a single value
        return EnvValue.StringValue(value)
    }

    init {
        loadEnv()
    }
}

class EnvFileNotFoundException : Exception("ENV: .env file not found")
class MissingEnvKeyException(key: EnvKey) : Exception("ENV: Missing required key: $key")
class MissingEnvKeysException(keys: Collection<EnvKey>) :
    Exception("ENV: Missing required keys: ${keys.joinToString()}")
class InvalidEnvValueTypeException(key: EnvKey, value: String) :
    Exception("ENV: Invalid value type for key $key: $value. Expected ${key.type}")