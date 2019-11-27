package de.ph1b.audiobook.features.bookPlaying

import android.app.Dialog
import android.os.Bundle
import androidx.core.view.isVisible
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import de.ph1b.audiobook.R
import de.ph1b.audiobook.data.repo.BookRepository
import de.ph1b.audiobook.injection.PrefKeys
import de.ph1b.audiobook.injection.appComponent
import de.ph1b.audiobook.misc.DialogController
import de.ph1b.audiobook.misc.DialogLayoutContainer
import de.ph1b.audiobook.misc.inflate
import de.ph1b.audiobook.persistence.pref.Pref
import de.ph1b.audiobook.playback.PlayerCommand
import de.ph1b.audiobook.playback.PlayerController
import kotlinx.android.synthetic.main.dialog_time_picker.*
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class JumpToPositionDialogController : DialogController() {

  @field:[Inject Named(PrefKeys.CURRENT_BOOK)]
  lateinit var currentBookIdPref: Pref<UUID>
  @Inject
  lateinit var repo: BookRepository
  @Inject
  lateinit var playerController: PlayerController

  override fun onCreateDialog(savedViewState: Bundle?): Dialog {
    appComponent.inject(this)

    val container =
      DialogLayoutContainer(activity!!.layoutInflater.inflate(R.layout.dialog_time_picker))

    // init
    val book = repo.bookById(currentBookIdPref.value)!!
    val duration = book.content.currentChapter.duration
    val position = book.content.positionInChapter
    val biggestHour = TimeUnit.MILLISECONDS.toHours(duration).toInt()
    val durationInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration).toInt()
    if (biggestHour == 0) {
      // sets visibility of hour related things to gone if max.hour is zero
      container.colon.isVisible = false
      container.numberHour.isVisible = false
    }

    // set maximum values
    container.numberHour.maxValue = biggestHour
    if (biggestHour == 0) {
      container.numberMinute.maxValue = TimeUnit.MILLISECONDS.toMinutes(duration).toInt()
    } else {
      container.numberMinute.maxValue = 59
    }

    // set default values
    val defaultHour = TimeUnit.MILLISECONDS.toHours(position).toInt()
    val defaultMinute = TimeUnit.MILLISECONDS.toMinutes(position).toInt() % 60
    container.numberHour.value = defaultHour
    container.numberMinute.value = defaultMinute

    container.numberHour.setOnValueChangedListener { _, _, newVal ->
      if (newVal == biggestHour) {
        container.numberMinute.maxValue = (durationInMinutes - newVal * 60) % 60
      } else {
        container.numberMinute.maxValue = 59
      }
    }

    container.numberMinute.setOnValueChangedListener { _, oldVal, newVal ->
      var hValue = container.numberHour.value

      // scrolling forward
      if (oldVal == 59 && newVal == 0) {
        container.numberHour.value = ++hValue
      }
      // scrolling backward
      if (oldVal == 0 && newVal == 59) {
        container.numberHour.value = --hValue
      }
    }

    return MaterialDialog(activity!!).apply {
      customView(view = container.containerView, scrollable = true)
      title(R.string.action_time_change)
      positiveButton(R.string.dialog_confirm) {
        val h = container.numberHour.value
        val m = container.numberMinute.value
        val newPosition = (m + 60 * h) * 60 * 1000L
        playerController.execute(PlayerCommand.SetPosition(newPosition, book.content.currentChapter.file))
      }
      negativeButton(R.string.dialog_cancel)
    }
  }
}
