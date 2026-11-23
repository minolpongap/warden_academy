package pg.wardenacademy.data.content

/** Course content, loaded from assets/course.json. Edit the JSON to update training material. */
data class Course(
    val title: String,
    val version: String,
    val disclaimer: String,
    val modules: List<TrainingModule>,
    val glossary: List<GlossaryTerm> = emptyList()
)

data class TrainingModule(
    val id: String,
    val number: Int,
    val title: String,
    val sections: String,
    val summary: String,
    val objectives: List<String> = emptyList(),
    val lessons: List<Lesson> = emptyList(),
    val keyPoints: List<String> = emptyList(),
    val tokPisin: String? = null,
    val quiz: List<Question> = emptyList()
)

data class Lesson(
    val heading: String,
    val reference: String? = null,
    val body: String = "",
    val bullets: List<String> = emptyList(),
    val note: String? = null
)

data class Question(
    val id: String,
    val prompt: String,
    val options: List<String>,
    val answerIndex: Int,
    val explanation: String,
    val reference: String? = null
)

data class GlossaryTerm(
    val term: String,
    val definition: String
)
