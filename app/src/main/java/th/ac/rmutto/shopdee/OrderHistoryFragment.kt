package th.ac.rmutto.shopdee

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.StrictMode
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.DecimalFormat
import java.text.NumberFormat


class HistoryOrderFragment : Fragment() {
    var layout: LinearLayout? = null
    var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_order_history, container, false)

        //For an synchronous task
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val sharedPrefer = requireContext().getSharedPreferences(
            "appPrefer", Context.MODE_PRIVATE
        )
        val custID = sharedPrefer?.getString("custIDPref", null)
        val token = sharedPrefer?.getString("tokenPref", null)

        layout = root.findViewById(R.id.layout)
        recyclerView = root.findViewById(R.id.recyclerView)

        showDataList(custID!!, token!!)

        return root
    }

    private fun showDataList(custID: String, token: String) {
        val data = ArrayList<Data>()
        val url: String = getString(R.string.root_url) + getString(R.string.history_url) + custID
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
                for (i in 0 until res.length()) {

                    val item: JSONObject = res.getJSONObject(i)
                    var orderDate = item.getString("orderDate")
                    if(orderDate == "null") orderDate = "-"
                    data.add(
                        Data(
                            item.getInt("orderID"),
                            orderDate,
                            item.getDouble("totalPrice"),
                            item.getInt("statusID")
                        )
                    )
                }
                recyclerView!!.adapter = DataAdapter(data)
            }else{
                hideAllViews()
            }
        }
    }


    internal class Data(
        var orderID: Int, var orderDate: String,
        var totalPrice: Double, var orderStatus: Int
    )
    internal inner class DataAdapter(private val list: List<Data>) :
        RecyclerView.Adapter<DataAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View = LayoutInflater.from(parent.context).inflate(
                R.layout.item_orderhistory,
                parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val formatter: NumberFormat = DecimalFormat("#,###")
            val status = arrayOf(
                "เลือกสินค้า", "รอการชำระเงิน", "กำลังตรวจสอบการชำระ", "ชำระแล้ว", "กำลังส่งสินค้า", "ส่งสินค้าแล้ว")
            val data = list[position]
            holder.data = data
            holder.textViewOrderID.text = data.orderID.toString()
            holder.textViewOrderDate.text = data.orderDate
            holder.textViewTotalPrice.text = "\u0E3F" + formatter.format(data.totalPrice)
            holder.textViewOrderStatus.text = status[data.orderStatus]


            if(data.orderStatus == 1){
                holder.textViewOrderStatus.setTextColor(Color.parseColor("#FF0000"))
            }else{
                holder.textViewOrderStatus.setTextColor(Color.parseColor("#000000"))
            }

            holder.linearLayout.setOnClickListener {
                /*
                Toast.makeText(context, "คุณเลือกรหัสการสั่งซื้อ " + holder.textViewOrderID.text,
                    Toast.LENGTH_LONG).show()
                */

                if(data.orderStatus == 1){
                    //Go to orderActivity
                    val intent = Intent(context, PaymentActivity::class.java)
                    intent.putExtra("orderID", data.orderID)
                    intent.putExtra("price", data.totalPrice)
                    startActivity(intent)
                }else{
                    //Go to orderActivity
                    val intent = Intent(context, OrderActivity::class.java)
                    intent.putExtra("orderID", data.orderID)
                    startActivity(intent)
                }

            }

            if(position % 2 == 0)
            {
                holder.linearLayout.setBackgroundResource(R.color.gray);
            }
            else
            {
                holder.linearLayout.setBackgroundResource(R.color.white);
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }

        internal inner class ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var data: Data? = null
            var textViewOrderID: TextView = itemView.findViewById(R.id.textViewOrderID)
            var textViewOrderDate: TextView = itemView.findViewById(R.id.textViewOrderDate)
            var textViewTotalPrice: TextView = itemView.findViewById(R.id.textViewTotalPrice)
            var textViewOrderStatus: TextView = itemView.findViewById(R.id.textViewOrderStatus)
            var linearLayout: LinearLayout = itemView.findViewById(R.id.linearLayout)

        }
    }

    // Function to hide all views
    private fun hideAllViews() {
        for (i in 0 until layout!!.childCount) {
            val childView = layout!!.getChildAt(i)
            childView.visibility = View.GONE // Or View.INVISIBLE if you want them to take up space
        }
    }
}