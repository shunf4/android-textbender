package sh.eliza.textbender

import android.content.Intent
import android.service.quicksettings.Tile

class BendClipboardLongClickTileService : TextbenderTileService() {
  override val desiredState: Int
    get() =
      if (preferencesSnapshot.clipboardDestinationLongClick != TextbenderPreferences.Destination.DISABLED) {
        Tile.STATE_INACTIVE
      } else {
        Tile.STATE_UNAVAILABLE
      }

  override fun onClick() {
    super.onClick()
    startActivityAndCollapse(
      Intent(this, BendClipboardLongClickActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
    )
  }
}
