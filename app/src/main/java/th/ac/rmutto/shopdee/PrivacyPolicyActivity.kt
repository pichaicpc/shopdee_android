package th.ac.rmutto.shopdee

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        // Set up the toolbar as the action bar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Change the title color
        toolbar.setTitleTextColor(resources.getColor(R.color.white, theme)) // Set the desired color

        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set the title if necessary
        supportActionBar?.title = "นโยบายความเป็นส่วนตัว"

        // Set the custom white back arrow drawable
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)

        val TextViewPrivacyPolicy: TextView = findViewById(R.id.TextViewPrivacyPolicy)
        TextViewPrivacyPolicy.movementMethod = ScrollingMovementMethod()
    }

    // Handle the back arrow click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Navigate back to the previous activity
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}