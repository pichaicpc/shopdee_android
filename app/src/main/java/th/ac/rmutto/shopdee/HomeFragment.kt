package th.ac.rmutto.shopdee

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.ArrayList


class HomeFragment : Fragment() {

    var recyclerView: RecyclerView? = null
    private var data = ArrayList<Data>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        //For an synchronous task
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        //List data
        recyclerView = root.findViewById(R.id.recyclerView)
        showDataList()

        return root
    }

    //show a data list
    private fun showDataList() {
        val url: String = getString(R.string.root_url) + getString(R.string.product_url)
        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder().url(url).get().build()
        val response = okHttpClient.newCall(request).execute()

        if (response.isSuccessful) {
            val res = JSONArray(response.body!!.string())
            if (res.length() > 0) {
                for (i in 0 until res.length()) {
                    val item: JSONObject = res.getJSONObject(i)
                    data.add(
                        Data(
                            item.getString("productID"),
                            item.getString("productName"),
                            item.getString("price"),
                            item.getString("quantity"),
                            item.getString("imageFile")
                        )
                    )
                }

                recyclerView!!.adapter = DataAdapter(data)
            } else {
                Toast.makeText(context, "ไม่สามารถแสดงข้อมูลได้", Toast.LENGTH_LONG).show()
            }
        }
    }


    internal class Data(
        var productID: String, var productName: String, var price: String,
        var quantity: String, var imageViewFile: String
    )

    internal inner class DataAdapter(private val list: List<Data>) :
        RecyclerView.Adapter<DataAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View = LayoutInflater.from(parent.context).inflate(
                R.layout.item_product,
                parent, false
            )
            return ViewHolder(view)
        }

        internal inner class ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var data: Data? = null
            var imageViewFile: ImageView = itemView.findViewById(R.id.imageViewFile)
            var productName: TextView = itemView.findViewById(R.id.textViewProductName)
            var price: TextView = itemView.findViewById(R.id.textViewPrice)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val data = list[position]
            holder.data = data
            val url = getString(R.string.root_url) +
                    getString(R.string.product_image_url) + data.imageViewFile

            Picasso.get().load(url).into(holder.imageViewFile)
            holder.productName.text = data.productName
            holder.price.text = "฿" + data.price

            holder.imageViewFile.setOnClickListener {
                val intent = Intent(context, ProductActivity::class.java)
                intent.putExtra("productID", data.productID)
                startActivity(intent)
            }

        }
    }

}