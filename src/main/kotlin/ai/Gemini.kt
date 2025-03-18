package ai

import com.google.genai.Client
import com.google.genai.ResponseStream
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.GenerateContentResponse

object Gemini {
    private val client: Client = Client.Builder().apiKey(Env.get(EnvKey.GOOGLE_API_KEY)).build()

    fun generate(model: String, content: String, config: GenerateContentConfig): GenerateContentResponse {
        return client.models.generateContent(model, content, config)
    }

    fun generate(content: String): ResponseStream<GenerateContentResponse> {
        val client = client.models.generateContentStream("gemini-2.0-flash", content + ". Don't use markdown syntax instead just write text paragraphs.", null)
        return client
    }
}
