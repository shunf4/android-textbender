package sh.eliza.textbender

import android.accessibilityservice.AccessibilityButtonController
import android.accessibilityservice.AccessibilityButtonController.AccessibilityButtonCallback
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "TextbenderService"

private val atomicInstance = AtomicReference<TextbenderService>()

class TextbenderService : AccessibilityService() {
  private val handlerThread = HandlerThread(TAG).apply { start() }
  val handler = Handler(handlerThread.looper)

  private val onPreferenceChangeListener: () -> Unit = { handler.post { resetFloatingButton() } }

  lateinit var windowManager: WindowManager

  private var snapshot: Snapshot? = null
  private val openYomichanStateMachine = AtomicReference<OpenYomichanStateMachine>()

  private var floatingButton: FloatingButton? = null

  override fun onCreate() {
    super.onCreate()
    windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
  }

  override fun onServiceConnected() {
    atomicInstance.set(this)

    accessibilityButtonController.registerAccessibilityButtonCallback(
      object : AccessibilityButtonCallback() {
        override fun onAvailabilityChanged(
          controller: AccessibilityButtonController,
          available: Boolean
        ) {}

        override fun onClicked(controller: AccessibilityButtonController) {
          if (TextbenderPreferences.createFromContext(applicationContext)
              .accessibilityShortcutEnabled
          ) {
            if (snapshot === null) {
              openOverlay()
            }
          }
        }
      }
    )

    handler.post { resetFloatingButton() }

    TextbenderPreferences.registerOnChangeListener(applicationContext, onPreferenceChangeListener)
  }

  override fun onUnbind(intent: Intent): Boolean {
    TextbenderPreferences.unregisterOnChangeListener(applicationContext, onPreferenceChangeListener)
    handlerThread.run {
      quitSafely()
      join()
    }
    atomicInstance.compareAndSet(this, null)
    openYomichanStateMachine.getAndSet(null)?.close()
    return false
  }

  override fun onDestroy() {
    super.onDestroy()
    snapshot?.close()
    floatingButton?.close()
  }

  override fun onAccessibilityEvent(event: AccessibilityEvent) {}

  override fun onInterrupt() {}

  fun openYomichan(text: CharSequence) {
    handler.post { handleOpenYomichan(text) }
  }

  fun openOverlay() {
    handler.post { handleOpenOverlay() }
  }

  private fun handleOpenYomichan(text: CharSequence) {
    openYomichanStateMachine
      .getAndSet(
        OpenYomichanStateMachine(this, text) {
          it.close()
          openYomichanStateMachine.compareAndSet(it, null)
        }
      )
      ?.close()
  }

  private fun handleOpenOverlay() {
    floatingButton?.close()
    floatingButton = null
    snapshot?.close()
    snapshot =
      Snapshot(applicationContext, windowManager, windows) {
        this.snapshot?.close()
        this.snapshot = null
        resetFloatingButton()
      }
  }

  private fun resetFloatingButton() {
    val enabled = TextbenderPreferences.createFromContext(applicationContext).floatingButtonEnabled
    if (enabled) {
      if (floatingButton === null) {
        floatingButton = FloatingButton(this)
      }
    } else {
      floatingButton?.close()
      floatingButton = null
    }
  }

  companion object {
    val instance: TextbenderService?
      get() = atomicInstance.get()
  }
}
