package org.clarkecollective.raderie.ui.links

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.ui.share.ShareActivity

class LinksActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_links)
    Logger.addLogAdapter(AndroidLogAdapter())

    Logger.d("Intent: $intent")
    handleIntent(intent)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
  }

  private fun handleIntent(intent: Intent) {
    val appLinkAction = intent.action
    val appLinkData: Uri? = intent.data

    Logger.d("App Link Action: %s  \n App Link Data: %s", appLinkAction, appLinkData)

    if (Intent.ACTION_VIEW == appLinkAction) {
      appLinkData?.lastPathSegment?.also { uuid ->
        Uri.parse("https://raderie.me")
          .buildUpon()
          .appendPath(uuid)
          .build().also { appData ->
            Logger.d(appData.lastPathSegment)
            val startShareIntent = Intent(this, ShareActivity::class.java)
            startShareIntent.putExtra(getString(R.string.CallingActivity), getString(R.string.LinksActivity))
            startShareIntent.putExtra(getString(R.string.frienduuid), appLinkData.lastPathSegment)
            startActivity(startShareIntent)
            Logger.d("Calling activity string ${getString(R.string.CallingActivity)}, Links activity string ${getString(R.string.LinksActivity)}")
          }

      }


    }
  }
}