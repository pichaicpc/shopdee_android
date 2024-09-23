package th.ac.rmutto.shopdee

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.TimeZone

class OrderActivity : AppCompatActivity() {
    var textViewOrderID: TextView? = null
    var textViewOrderDate: TextView? = null
    var textViewCustomerName: TextView? = null
    var textViewAddress: TextView? = null
    var textViewTotalPrice: TextView? = null
    var recyclerView: RecyclerView? = null
    var buttonChat: Button? = null
    var totalPrice: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        // Set up the toolbar as the action bar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Change the title color
        toolbar.setTitleTextColor(resources.getColor(R.color.white, theme)) // Set the desired color

        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set the title if necessary
        supportActionBar?.title = "รายการสั่งซื้อ"

        // Set the custom white back arrow drawable
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)

        //For an synchronous task
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        //Receive variables from caller
        val orderID = intent.extras!!.getInt("orderID")

        val sharedPrefer = getSharedPreferences("appPrefer", Context.MODE_PRIVATE)
        val custID = sharedPrefer?.getString("custIDPref", null)?.toInt()
        val token = sharedPrefer?.getString("tokenPref", null)

        textViewOrderID = findViewById(R.id.textViewOrderID)
        textViewOrderDate = findViewById(R.id.textViewOrderDate)
        textViewCustomerName = findViewById(R.id.textViewCustomerName)
        textViewAddress = findViewById(R.id.textViewAddress)
        textViewTotalPrice = findViewById(R.id.textViewTotalPrice)
        buttonChat = findViewById(R.id.buttonChat)
        recyclerView = findViewById(R.id.recyclerView)

        showOrderInfo(custID!!, orderID, token!!)
        showOrderList(custID, orderID, token)

        buttonChat!!.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("empID", -1)
            intent.putExtra("orderID", orderID)

            startActivity(intent)
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

    private fun showOrderInfo(custID: Int, orderID: Int, token: String){

        val url: String = getString(R.string.root_url) + getString(R.string.orderinfo_url) + custID + "/" + orderID
        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        val response = okHttpClient.newCall(request).execute()

        if (response.isSuccessful) {
            val formatter: NumberFormat = DecimalFormat("#,###")
            val calendar = Calendar.getInstance(TimeZone.getDefault())
            val currentTime = calendar.get(Calendar.YEAR).toString() + "-" +
                    calendar.get(Calendar.MONTH).toString() + "-" +
                    calendar.get(Calendar.DATE).toString() + " " +
                    Calendar.HOUR + ":" +
                    Calendar.MINUTE + ":" +
                    Calendar.SECOND
            val res = JSONArray(response.body!!.string())
            val data: JSONObject = res.getJSONObject(0)
            if (data.length() > 0) {
                textViewOrderID?.text = data.getString("orderID")
                textViewOrderDate?.text= currentTime
                textViewCustomerName?.text = data.getString("firstName") + " " +
                        data.getString("lastName")
                var address = data.getString("address")
                if(address.equals("null"))address = ""
                textViewAddress?.text = address
                totalPrice =  data.getDouble("totalPrice")
                textViewTotalPrice?.text = "\u0E3F" + formatter.format(totalPrice)

            }
        }

    }

    private fun showOrderList(custID: Int, orderID: Int, token: String){
        val data = ArrayList<Data>()
        val url: String = getString(R.string.root_url) +
                getString(R.string.orderdetail_url) + custID + "/" + orderID
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
                val formatter: NumberFormat = DecimalFormat("#,###")
                for (i in 0 until res.length()) {
                    val item: JSONObject = res.getJSONObject(i)
                    val order = (i + 1).toString()
                    val productName = item.getString("productName")
                    val quantity = item.getString("quantity")
                    val price = item.getDouble("price")

                    data.add(
                        Data(order, productName, quantity,
                            "\u0E3F" + formatter.format(price),
                            "\u0E3F" + formatter.format(price * quantity.toInt() )
                        )
                    )

                }
                recyclerView!!.adapter = DataAdapter(data)
            }
        }
    }


    internal class Data(
        var order: String, var productName: String,
        var quantity: String, var price: String, var sum: String
    )

    internal inner class DataAdapter(private val list: List<Data>) :
        RecyclerView.Adapter<DataAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View = LayoutInflater.from(parent.context).inflate(
                R.layout.item_orderdetail,
                parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val data = list[position]
            holder.data = data
            holder.textViewOrder.text = data.order
            holder.textViewProductName.text = data.productName
            holder.textViewQuantity.text = data.quantity
            holder.textViewPrice.text = data.price
            holder.textViewSum.text = data.sum
        }

        override fun getItemCount(): Int {
            return list.size
        }

        internal inner class ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var data: Data? = null
            var textViewOrder: TextView = itemView.findViewById(R.id.textViewOrder)
            var textViewProductName: TextView = itemView.findViewById(R.id.textViewProductName)
            var textViewQuantity: TextView = itemView.findViewById(R.id.textViewQuantity)
            var textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
            var textViewSum: TextView = itemView.findViewById(R.id.textViewSum)

        }

    }
}