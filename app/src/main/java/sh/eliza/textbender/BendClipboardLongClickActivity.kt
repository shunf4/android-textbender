package sh.eliza.textbender

import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BendClipboardLongClickActivity : AppCompatActivity() {
  private val toaster = Toaster(this)

  override fun onWindowFocusChanged(hasFocus: Boolean) {
//    toaster.show((intent.getParcelableExtra<ComponentName>(Intent.EXTRA_COMPONENT_NAME))!!.shortClassName, Toast.LENGTH_SHORT)
    val preferences = TextbenderPreferences.getInstance(this).snapshot
    if (hasFocus) {
      val text =
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
          .primaryClip
          ?.getItemAt(0)
          ?.coerceToText(this)
      val dest = when ((intent.getParcelableExtra<ComponentName>(Intent.EXTRA_COMPONENT_NAME))?.shortClassName) {
        ".BendClipboard2TileService" -> preferences.clipboardDestinationLongClick2
        ".BendClipboardTileService" -> preferences.clipboardDestinationLongClick
        else -> null
      }
      if (dest == null) {
        startActivity(
          Intent(this, SettingsActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        )
      } else if (dest == TextbenderPreferences.Destination.DISABLED) {
        toaster.show(getString(R.string.clipboard_not_configured), Toast.LENGTH_SHORT)
        startActivity(
          Intent(this, SettingsActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        )
      } else {
        Textbender.handleText(this, toaster, preferences, dest, text)
      }
      finish()
    }
  }
}
