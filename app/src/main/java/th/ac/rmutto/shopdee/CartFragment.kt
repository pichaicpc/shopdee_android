package th.ac.rmutto.shopdee

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.TimeZone


class CartFragment : Fragment() {
    var layout: LinearLayout? = null
    var textViewOrderID: TextView? = null
    var textViewOrderDate: TextView? = null
    var textViewCustomerName: TextView? = null
    var textViewAddress: TextView? = null
    var textViewTotalPrice: TextView? = null
    var buttonConfirmOrder: Button? = null
    var textViewNoRecord: TextView? = null
    var recyclerView: RecyclerView? = null
    var totalPrice: Double = 0.0
    var orderID: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_cart, container, false)

        //For an synchronous task
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        //get custID from shared preference
        val sharedPrefer = requireContext().getSharedPreferences(
            "appPrefer", Context.MODE_PRIVATE)
        val custID = sharedPrefer?.getString("custIDPref", null)?.toInt()
        val token = sharedPrefer?.getString("tokenPref", null)

        layout = root.findViewById(R.id.layout)
        textViewOrderID = root.findViewById(R.id.textViewOrderID)
        textViewOrderDate = root.findViewById(R.id.textViewOrderDate)
        textViewCustomerName = root.findViewById(R.id.textViewCustomerName)
        textViewAddress = root.findViewById(R.id.textViewAddress)
        textViewTotalPrice = root.findViewById(R.id.textViewTotalPrice)
        textViewNoRecord = root.findViewById(R.id.textViewNoRecord)
        recyclerView = root.findViewById(R.id.recyclerView)
        buttonConfirmOrder = root.findViewById(R.id.buttonConfirmOrder)

        showOrderInfo(custID!!, token!!)

        showOrderList(custID, token)

        buttonConfirmOrder?.setOnClickListener {
            confirmOrder(custID, token)
            val intent = Intent(context, PaymentActivity::class.java)
            intent.putExtra("orderID", orderID!!)
            intent.putExtra("price", totalPrice)
            startActivity(intent)
        }

        return root
    }

    private fun showOrderInfo(custID: Int, token: String){
        val url: String = getString(R.string.root_url) + getString(R.string.cart_url) + custID
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

            if (res.length() > 0) {
                val data: JSONObject = res.getJSONObject(0)
                orderID = data.getInt("orderID")
                textViewOrderID?.text = data.getString("orderID")
                textViewOrderDate?.text= currentTime
                textViewCustomerName?.text = data.getString("firstName") + " " +
                        data.getString("lastName")
                var address = data.getString("address")
                if(address.equals("null"))address = ""
                textViewAddress?.text = address
                totalPrice =  data.getDouble("totalPrice")
                textViewTotalPrice?.text = "฿" + formatter.format(totalPrice)
                buttonConfirmOrder?.visibility = View.VISIBLE
            }else{
                hideAllViews()
            }
        }
    }

    private fun showOrderList(custID: Int, token: String) {
        val data = ArrayList<Data>()
        val url: String = getString(R.string.root_url) +
                getString(R.string.orderdetail_url) + custID.toString() + "/" + orderID.toString()
        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get().
            build()
        val response = okHttpClient.newCall(request).execute()

        if (response.isSuccessful) {
            val res = JSONArray(response.body!!.string())
            val formatter: NumberFormat = DecimalFormat("#,###")
            for (i in 0 until res.length()) {
                val item: JSONObject = res.getJSONObject(i)
                val order = (i + 1).toString()
                val productName = item.getString("productName")
                val quantity = item.getString("quantity")
                val price = item.getDouble("price")
                data.add(
                    Data(order, productName, quantity,
                        "฿" + formatter.format(price),
                        "฿" + formatter.format(price * quantity.toInt() )
                    )
                )
            }
            recyclerView!!.adapter = DataAdapter(data)
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

        internal inner class ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var data: Data? = null
            var textViewOrder: TextView = itemView.findViewById(R.id.textViewOrder)
            var textViewProductName: TextView = itemView.findViewById(R.id.textViewProductName)
            var textViewQuantity: TextView = itemView.findViewById(R.id.textViewQuantity)
            var textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
            var textViewSum: TextView = itemView.findViewById(R.id.textViewSum)
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
    }


    private fun confirmOrder(custID: Int, token: String){
        val url = getString(R.string.root_url) + getString(R.string.confirmorder_url)
        val okHttpClient = OkHttpClient()
        val formBody: RequestBody = FormBody.Builder()
            .add("orderID",orderID.toString())
            .add("custID",custID.toString())
            .build()
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .post(formBody)
            .build()
        val response = okHttpClient.newCall(request).execute()

        if(response.isSuccessful){
            val obj = JSONObject(response.body!!.string())
            val message = obj["message"].toString()
            val status = obj["status"].toString()

            if (status == "false") {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Function to hide all views
    private fun hideAllViews() {
        for (i in 0 until layout!!.childCount) {
            val childView = layout!!.getChildAt(i)
            childView.visibility = View.GONE // Or View.INVISIBLE if you want them to take up space
        }
        textViewNoRecord!!.visibility = View.VISIBLE
    }
}