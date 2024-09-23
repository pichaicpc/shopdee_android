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
import android.widget.ImageView
import android.widget.LinearLayout
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

class ChatListFragment : Fragment() {
    var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_chat_list, container, false)

        //For an synchronous task
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val sharedPrefer = requireContext().getSharedPreferences(
            "appPrefer", Context.MODE_PRIVATE
        )
        val custID = sharedPrefer?.getString("custIDPref", null)?.toInt()
        val token = sharedPrefer?.getString("tokenPref", null)

        recyclerView = root.findViewById(R.id.recyclerView)
        showDataList(custID!!, token!!)

        return root
    }

    private fun showDataList(custID: Int, token: String) {
        val data = ArrayList<Data>()
        val url: String = getString(R.string.root_url) + getString(R.string.chat_list_url) + custID
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
                    data.add(
                        Data(
                            item.getInt("empID"),
                            item.getString("message"),
                            item.getString("chatTime"),
                            item.getString("imageFile"),
                            item.getString("employee"),
                            item.getInt("orderID")
                        )
                    )
                }
                recyclerView!!.adapter = DataAdapter(data)
            }
        }
    }


    internal class Data(
        var empID: Int,var message: String, var chatTime: String,
        var imageFile: String, var employee: String, var orderID: Int
    )
    internal inner class DataAdapter(private val list: List<Data>) :
        RecyclerView.Adapter<DataAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View = LayoutInflater.from(parent.context).inflate(
                R.layout.item_chat_list,
                parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = list[position]
            holder.data = data

            val url = getString(R.string.root_url) +
                    getString(R.string.employee_image_url) + data.imageFile
            Picasso.get().load(url).into(holder.imageView)

            holder.textViewEmployee.text = data.employee
            holder.textViewMessage.text = data.message
            holder.textViewChatTime.text = data.chatTime
            holder.linearLayout.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("empID", data.empID)
                intent.putExtra("orderID", data.orderID)
                startActivity(intent)

            }

        }

        override fun getItemCount(): Int {
            return list.size
        }

        internal inner class ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var data: Data? = null
            var imageView: ImageView = itemView.findViewById(R.id.imageView)
            var textViewEmployee: TextView = itemView.findViewById(R.id.textViewEmployee)
            var textViewMessage: TextView = itemView.findViewById(R.id.textViewMessage)
            var textViewChatTime: TextView = itemView.findViewById(R.id.textViewChatTime)
            var linearLayout: LinearLayout = itemView.findViewById(R.id.linearLayout)
        }

    }

}