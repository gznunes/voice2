package de.ph1b.audiobook.features.bookCategory

import de.ph1b.audiobook.data.repo.BookRepository
import de.ph1b.audiobook.features.bookOverview.GridMode
import de.ph1b.audiobook.features.bookOverview.list.BookComparator
import de.ph1b.audiobook.features.bookOverview.list.BookOverviewModel
import de.ph1b.audiobook.features.bookOverview.list.header.BookOverviewCategory
import de.ph1b.audiobook.features.gridCount.GridCount
import de.ph1b.audiobook.injection.PrefKeys
import de.ph1b.audiobook.misc.Observables
import de.ph1b.audiobook.persistence.pref.Pref
import io.reactivex.Observable
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class BookCategoryViewModel
@Inject constructor(
  private val repo: BookRepository,
  @Named(PrefKeys.CURRENT_BOOK)
  private val currentBookIdPref: Pref<UUID>,
  @Named(PrefKeys.GRID_MODE)
  private val gridModePref: Pref<GridMode>,
  private val gridCount: GridCount,
  private val comparatorPrefForCategory: @JvmSuppressWildcards Map<BookOverviewCategory, Pref<BookComparator>>
) {

  lateinit var category: BookOverviewCategory

  private fun comparatorPref(): Pref<BookComparator> = comparatorPrefForCategory.getValue(category)

  fun get(): Observable<BookCategoryState> {
    val comparatorStream = comparatorPref().stream
    return Observables.combineLatest(
      gridModePref.stream,
      repo.booksStream(),
      comparatorStream
    ) { gridMode, books, comparator ->
      val gridColumnCount = gridCount.gridColumnCount(gridMode)
      val currentBookId = currentBookIdPref.value
      val models = books.asSequence()
        .filter(category.filter)
        .sortedWith(comparator)
        .map { book ->
          BookOverviewModel(
            book = book,
            isCurrentBook = book.id == currentBookId,
            useGridView = gridColumnCount > 1
          )
        }
        .toList()
      BookCategoryState(gridColumnCount, models)
    }
  }

  fun sort(comparator: BookComparator) {
    comparatorPref().value = comparator
  }

  fun bookSorting(): BookComparator {
    return comparatorPref().value
  }
}

data class BookCategoryState(
  val gridColumnCount: Int,
  val models: List<BookOverviewModel>
)
