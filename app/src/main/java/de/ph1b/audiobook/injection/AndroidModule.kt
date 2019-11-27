package de.ph1b.audiobook.injection

import android.app.ActivityManager
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.hardware.SensorManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.os.PowerManager
import android.telephony.TelephonyManager
import android.view.WindowManager
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import de.paulwoitaschek.chapterreader.ChapterReader
import de.paulwoitaschek.chapterreader.ChapterReaderFactory
import de.ph1b.audiobook.covercolorextractor.CoverColorExtractor
import javax.inject.Singleton

/**
 * Module providing Android SDK Related instances.
 */
@Module
object AndroidModule {

  @Provides
  @JvmStatic
  fun provideContext(app: Application): Context = app

  @Provides
  @Singleton
  @JvmStatic
  fun provideAudioManager(context: Context) =
    context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

  @Provides
  @Singleton
  @JvmStatic
  fun provideActivityManager(context: Context) =
    context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

  @Provides
  @Singleton
  @JvmStatic
  fun provideTelephonyManager(context: Context) =
    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

  @Provides
  @Singleton
  @JvmStatic
  fun provideConnectivityManager(context: Context) =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  @Provides
  @JvmStatic
  fun provideWindowManager(context: Context) =
    context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

  @Provides
  @Singleton
  @JvmStatic
  fun provideNotificationManager(context: Context) =
    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  @Provides
  @Singleton
  @JvmStatic
  fun provideSensorManager(context: Context) =
    context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?

  @Provides
  @Singleton
  @JvmStatic
  fun providePowerManager(context: Context) =
    context.getSystemService(Context.POWER_SERVICE) as PowerManager

  @Provides
  @Singleton
  @JvmStatic
  fun provideMoshi(): Moshi = Moshi.Builder().build()

  @Provides
  @Singleton
  @JvmStatic
  fun provideChapterReader(): ChapterReader = ChapterReaderFactory.create()

  @Provides
  @Singleton
  @JvmStatic
  fun provideCoverColorExtractor(): CoverColorExtractor {
    return CoverColorExtractor()
  }
}
