package com.example.manadeliverybellempally.data.repository

import com.example.manadeliverybellempally.data.model.Product
import com.example.manadeliverybellempally.data.model.Vendor
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class AiAssistantService {

    // API key is sourced from BuildConfig (set in build.gradle.kts)
    // Never hardcode API keys in source code!
    private val apiKey = com.example.manadeliverybellempally.BuildConfig.GEMINI_API_KEY

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    suspend fun getSemanticRecommendations(
        query: String,
        vendors: List<Vendor>,
        products: List<Product>
    ): List<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY") {
            // Failsafe: Return empty if key not provided
            return@withContext emptyList()
        }

        try {
            val contextBuilder = StringBuilder()
            contextBuilder.append("You are an AI assistant for a hyperlocal delivery app called Mana Delivery in Bellempally.\n")
            contextBuilder.append("The user wants: '$query'.\n\n")
            contextBuilder.append("Here are the available products across our vendors:\n")
            
            val activeProducts = products.take(150)
            activeProducts.forEach { p ->
                val vendor = vendors.find { it.id == p.vendorId }
                val vendorName = vendor?.storeName ?: "Unknown Store"
                contextBuilder.append("- Product ID: ${p.id} | Name: ${p.name} | Desc: ${p.description} | Store: $vendorName\n")
            }

            contextBuilder.append("\nBased on the user's request, semantic meaning, and the available products, select up to 10 best matching product IDs.")
            contextBuilder.append("\nReturn EXACTLY a JSON array of strings containing the product IDs and NOTHING ELSE. Example: [\"prod1\", \"prod2\"]")

            val prompt = contextBuilder.toString()
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text?.trim() ?: "[]"

            val cleanJson = responseText
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val jsonArray = JSONArray(cleanJson)
            val resultIds = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                resultIds.add(jsonArray.getString(i))
            }
            
            return@withContext resultIds

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
}