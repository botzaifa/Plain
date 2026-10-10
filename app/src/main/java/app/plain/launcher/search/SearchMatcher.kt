package app.plain.launcher.search

import java.text.Normalizer

data class SearchCandidate(
    val packageName: String,
    val label: String,
)

object SearchMatcher {
    fun match(query: String, candidates: List<SearchCandidate>): List<SearchCandidate> {
        val q = normalize(query.trim())
        if (q.isEmpty()) return emptyList()
        return candidates
            .mapNotNull { candidate ->
                val label = normalize(candidate.label)
                val rank = when {
                    label.startsWith(q) -> 0
                    label.split(' ').any { it.startsWith(q) } -> 1
                    label.contains(q) -> 2
                    else -> null
                }
                rank?.let { candidate to it }
            }
            .sortedWith(
                compareBy<Pair<SearchCandidate, Int>> { it.second }
                    .thenBy { it.first.label.lowercase() },
            )
            .map { it.first }
    }

    private fun normalize(input: String): String {
        val nfd = Normalizer.normalize(input, Normalizer.Form.NFD)
        return nfd.replace("\\p{Mn}+".toRegex(), "").lowercase()
    }
}
