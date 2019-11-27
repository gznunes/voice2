package de.ph1b.audiobook.playback

import de.ph1b.audiobook.data.repo.BookRepository
import de.ph1b.audiobook.injection.PerService
import de.ph1b.audiobook.injection.PrefKeys
import de.ph1b.audiobook.misc.latestAsFlow
import de.ph1b.audiobook.persistence.pref.Pref
import de.ph1b.audiobook.playback.utils.ChangeNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

/**
 * Notifies about changes upon android auto connection.
 */
@PerService
class NotifyOnAutoConnectionChange @Inject constructor(
  private val changeNotifier: ChangeNotifier,
  private val repo: BookRepository,
  @Named(PrefKeys.CURRENT_BOOK)
  private val currentBookIdPref: Pref<UUID>,
  private val autoConnection: AndroidAutoConnectedReceiver
) {

  private var job: Job? = null

  fun listen() {
    if (job?.isActive != true) {
      job = GlobalScope.launch(Dispatchers.Main) {
        autoConnection.stream.latestAsFlow()
          .filter { it }
          .collect {
            // display the current book but don't play it
            val book = repo.bookById(currentBookIdPref.value)
            if (book != null) {
              changeNotifier.notify(ChangeNotifier.Type.METADATA, book, true)
            }
          }
      }
    }
  }

  fun unregister() {
    job?.cancel()
  }
}
