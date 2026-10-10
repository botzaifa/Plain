package app.plain.launcher.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.plain.launcher.data.AppRepository
import app.plain.launcher.data.DisplayApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SearchViewModel(repository: AppRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    val queryText: StateFlow<String> = query.asStateFlow()

    val results: StateFlow<List<DisplayApp>> =
        combine(query, repository.searchCandidates()) { q, candidates ->
            SearchMatcher.match(
                q,
                candidates.map { SearchCandidate(it.packageName, it.label) },
            ).map { matched ->
                candidates.first { it.packageName == matched.packageName }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        query.value = value
    }
}
