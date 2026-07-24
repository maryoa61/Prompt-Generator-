package com.example.domain.usecase

import android.content.Context
import android.net.Uri
import android.text.format.DateFormat
import com.example.data.local.db.PromptEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

enum class ExportFormat(val extension: String, val mimeType: String, val displayName: String) {
    JSON("json", "application/json", "JSON (.json)"),
    TXT("txt", "text/plain", "Plain Text (.txt)")
}

@Singleton
class ExportPromptHistoryUseCase @Inject constructor() {

    suspend fun exportToJson(context: Context, prompts: List<PromptEntity>, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            for (item in prompts) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("style", item.styleName)
                    put("rawInput", item.rawInput)
                    put("keywords", item.keywords)
                    put("fullGeneratedPrompt", item.fullGeneratedPrompt)
                    put("timestamp", item.timestamp)
                    put("isFavorite", item.isFavorite)
                }
                jsonArray.put(obj)
            }
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(jsonArray.toString(2).toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun exportToText(context: Context, prompts: List<PromptEntity>, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val sb = StringBuilder()
            val dateStr = DateFormat.format("MMM dd, yyyy • HH:mm", Date()).toString()
            sb.appendLine("========================================")
            sb.appendLine("PROMPT GENERATOR - HISTORY EXPORT")
            sb.appendLine("Export Date: $dateStr")
            sb.appendLine("Total Prompts: ${prompts.size}")
            sb.appendLine("========================================\n")

            prompts.forEachIndexed { index, item ->
                val itemDate = DateFormat.format("MMM dd, yyyy • HH:mm", Date(item.timestamp)).toString()
                sb.appendLine("--- [${index + 1}] ${item.title.ifBlank { "Untitled Prompt" }} ---")
                sb.appendLine("Category: ${item.styleName}")
                sb.appendLine("Date: $itemDate")
                sb.appendLine("Favorite: ${if (item.isFavorite) "Yes" else "No"}")
                if (item.rawInput.isNotBlank()) sb.appendLine("Raw Input: ${item.rawInput}")
                if (item.keywords.isNotBlank()) sb.appendLine("Keywords: ${item.keywords}")
                sb.appendLine("\n[PROMPT CONTENT]:")
                sb.appendLine(item.fullGeneratedPrompt)
                sb.appendLine("\n" + "=".repeat(40) + "\n")
            }

            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(sb.toString().toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
