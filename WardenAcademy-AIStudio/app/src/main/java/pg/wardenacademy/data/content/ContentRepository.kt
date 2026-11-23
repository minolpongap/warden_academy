package pg.wardenacademy.data.content

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Loads assets/course.json using Android's built-in org.json (no extra libraries). */
class ContentRepository(private val context: Context) {

    val course: Course by lazy {
        val text = context.assets.open("course.json").bufferedReader().use { it.readText() }
        parseCourse(JSONObject(text))
    }

    fun module(id: String): TrainingModule? = course.modules.firstOrNull { it.id == id }

    private fun parseCourse(o: JSONObject) = Course(
        title = o.getString("title"),
        version = o.optString("version", "1.0"),
        disclaimer = o.optString("disclaimer", ""),
        modules = o.getJSONArray("modules").objects().map(::parseModule),
        glossary = o.optJSONArray("glossary").objects().map {
            GlossaryTerm(it.getString("term"), it.getString("definition"))
        }
    )

    private fun parseModule(o: JSONObject) = TrainingModule(
        id = o.getString("id"),
        number = o.getInt("number"),
        title = o.getString("title"),
        sections = o.optString("sections", ""),
        summary = o.optString("summary", ""),
        objectives = o.optJSONArray("objectives").strings(),
        lessons = o.optJSONArray("lessons").objects().map {
            Lesson(
                heading = it.getString("heading"),
                reference = it.optStringOrNull("reference"),
                body = it.optString("body", ""),
                bullets = it.optJSONArray("bullets").strings(),
                note = it.optStringOrNull("note")
            )
        },
        keyPoints = o.optJSONArray("keyPoints").strings(),
        tokPisin = o.optStringOrNull("tokPisin"),
        quiz = o.optJSONArray("quiz").objects().map {
            Question(
                id = it.getString("id"),
                prompt = it.getString("prompt"),
                options = it.getJSONArray("options").strings(),
                answerIndex = it.getInt("answerIndex"),
                explanation = it.optString("explanation", ""),
                reference = it.optStringOrNull("reference")
            )
        }
    )
}

internal fun JSONArray?.objects(): List<JSONObject> =
    if (this == null) emptyList() else List(length()) { getJSONObject(it) }

internal fun JSONArray?.strings(): List<String> =
    if (this == null) emptyList() else List(length()) { getString(it) }

internal fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) getString(key) else null
