package th.ac.rmutto.shopdee

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import th.ac.rmutto.shopdee.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_cart,
                R.id.navigation_histry, R.id.navigation_dashboard,
                R.id.navigation_customer
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //get custID from shared preference
        val sharedPrefer = this.getSharedPreferences(
            "appPrefer", Context.MODE_PRIVATE)
        val custID = sharedPrefer?.getString("custIDPref", null)?.toInt()
        val token = sharedPrefer?.getString("tokenPref", null)

        //show badge
        val count = getItemCount(custID!!, token!!)
        if (count == 0) {
            navView.removeBadge(R.id.navigation_cart)
        } else {
            val badge = navView.getOrCreateBadge(R.id.navigation_cart) // previously showBadge
            badge.number = count
            badge.backgroundColor = getColor(R.color.yellow)
            badge.badgeTextColor = getColor(R.color.black)
        }

    }


    private fun getItemCount(custID: Int, token: String): Int {
        var itemCount = 0
        val url: String = getString(R.string.root_url) + getString(R.string.cart_url) + custID
        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            val res = JSONArray(response.body!!.string())
            if (res.length() > 0) {
                val item: JSONObject = res.getJSONObject(0)
                itemCount = item.getInt("itemCount")
            }
        }
        return itemCount
    }
}