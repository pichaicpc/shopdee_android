package th.ac.rmutto.shopdee

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.text.DecimalFormat
import java.text.NumberFormat

class PaymentActivity : AppCompatActivity() {
    var txtPrice: TextView? = null
    var imageViewSlip: ImageView? = null
    var buttonPayment: Button? = null
    var file: File? = null
    var custID: Int? = null
    var token: String? = null
    var orderID: Int? = null
    var price: Double = 0.0
    var floatingActionButton: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // Set up the toolbar as the action bar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Change the title color
        toolbar.setTitleTextColor(resources.getColor(R.color.white, theme)) // Set the desired color

        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set the title if necessary
        supportActionBar?.title = "ชำระค่าสินค้า"

        // Set the custom white back arrow drawable
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_circle_left_24)

        //allow network operations (APIs) run on a main (UI) tread.
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val sharedPrefer = getSharedPreferences("appPrefer", Context.MODE_PRIVATE)
        custID = sharedPrefer?.getString("custIDPref", null)?.toInt()
        token = sharedPrefer?.getString("tokenPref", null)

        //receive orderID and price from previous page
        orderID = intent.extras!!.getInt("orderID")
        price = intent.extras!!.getDouble("price")

        //refer to UI components from layout XML file
        txtPrice = findViewById(R.id.txtPrice)
        floatingActionButton = findViewById(R.id.floatingActionButton)
        imageViewSlip = findViewById(R.id.imageViewSlip)
        buttonPayment = findViewById(R.id.buttonPayment)
        val formatter: NumberFormat = DecimalFormat("#,###")
        txtPrice!!.text = "฿" + formatter.format(price)


        //upload or pick a picture
        val launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val uri = it.data?.data!!
                    val path = RealPathUtil.getRealPath(applicationContext, uri)
                    file = File(path.toString())
                    imageViewSlip?.setImageURI(uri)
                }
            }

        //click on a camera button
        floatingActionButton?.setOnClickListener {
            ImagePicker.Companion.with(this)
                .crop()
                .cropOval()
                .maxResultSize(480, 480)
                .provider(ImageProvider.BOTH) //Or bothCameraGallery()
                .createIntentFromDialog { launcher.launch(it) }
        }

        //click on a payment button
        buttonPayment?.setOnClickListener {
            payment()
        }
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

    //payment function
    private fun payment()
    {
        val url: String = getString(R.string.root_url) + getString(R.string.payment_url)
        val okHttpClient = OkHttpClient()
        val formBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("custID", custID!!.toString())
            .addFormDataPart("orderID", orderID!!.toString())
            .addFormDataPart("price", price.toString())
            .addFormDataPart("slipFile", file?.name,
                RequestBody.create("application/octet-stream".toMediaTypeOrNull(), file!!))
            .build()

        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .post(formBody)
            .build()
        val response = okHttpClient.newCall(request).execute()

        if (response.isSuccessful) {
            val data = JSONObject(response.body!!.string())
            if (data.length() > 0) {
                Toast.makeText(this, "ทำการชำระเงินสำเร็จแล้ว", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        }

    }

}