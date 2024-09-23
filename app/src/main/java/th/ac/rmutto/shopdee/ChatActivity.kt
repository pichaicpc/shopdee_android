package th.ac.rmutto.shopdee

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {
    var recyclerView: RecyclerView? = null
    var textViewMessage: EditText? = null
    val mHandler = Handler()
    var custID: Int? = null
    var empID: Int? = null
    var orderID: Int? = null
    var token: String? = null
    var pastCount = 0
    var currentCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Set up the toolbar as the action bar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Change the title color
        toolbar.setTitleTextColor(resources.getColor(R.color.white, theme)) // Set the desired color

        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set the title if necessary
        supportActionBar?.title = "แชต"

        // Set the custom white back arrow drawable
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)

        //For an synchronous task
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val sharedPrefer = getSharedPreferences("appPrefer", Context.MODE_PRIVATE)
        custID = sharedPrefer?.getString("custIDPref", null)?.toInt()
        token = sharedPrefer?.getString("tokenPref", null)

        empID = intent.extras!!.getInt("empID")
        orderID = intent.extras!!.getInt("orderID")


        recyclerView = findViewById(R.id.recyclerView)
        textViewMessage = findViewById(R.id.textViewMessage)
        val buttonSend: ImageButton = findViewById((R.id.buttonSend))

        showDataList()

        buttonSend.setOnClickListener {
            postMessage()
        }

        textViewMessage!!.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                postMessage()
                true
            } else {
                false
            }
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

    fun showDataList() {
        val data = ArrayList<Data>()
        val url: String = getString(R.string.root_url) + getString(R.string.chat_show_url)
        val okHttpClient = OkHttpClient()
        val formBody: RequestBody = FormBody.Builder()
            .add("custID", custID.toString())
            .add("empID", empID.toString())
            .add("orderID", orderID.toString())
            .build()
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .post(formBody)
            .build()
        val response = okHttpClient.newCall(request).execute()

        try{
            if (response.isSuccessful) {
                val res = JSONArray(response.body!!.string())
                if (res.length() > 0) {
                    for (i in 0 until res.length()) {
                        val item: JSONObject = res.getJSONObject(i)
                        data.add(
                            Data(
                                item.getString("message"),
                                item.getString("chatTime"),
                                item.getString("sender"),
                                item.getString("orderID"),
                                item.getString("imageFile")
                            )
                        )
                    }

                    currentCount = data.count()

                    if (pastCount != currentCount) {
                        recyclerView!!.adapter = DataAdapter(data)
                        recyclerView!!.scrollToPosition(data.count() - 1)
                        pastCount = currentCount
                    }
                }
            }
        }catch (_: Exception){

        }
    }

    internal class Data(
        var message: String, var chatTime: String,
        var sender: String, var orderID: String, var imageFile: String
    )

    internal inner class DataAdapter(private val list: List<Data>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemViewType(position: Int): Int {
            val data = list[position]
            if(data.sender == "c"){
                return 1
            }else{
                return 0
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            if (viewType === 1) {
                val view: View = inflater.inflate(R.layout.item_chat_customer, parent, false)
                return CustomerViewHolder(view)
            } else {
                val view: View = inflater.inflate(R.layout.item_chat_employee, parent, false)
                return EmployeeViewHolder(view)
            }

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val data = list[position]

            if (holder.itemViewType === 1) {
                var holder = holder as CustomerViewHolder

                holder.textViewMessage.text = data.message
                holder.textViewChatTime.text = data.chatTime
            } else {
                val holder = holder as EmployeeViewHolder

                val url = getString(R.string.root_url) +
                        getString(R.string.employee_image_url) + data.imageFile
                Picasso.get().load(url).into(holder.imageView)
                holder.textViewMessage.text = data.message
                holder.textViewChatTime.text = data.chatTime
            }

        }

        override fun getItemCount(): Int {
            return list.size
        }

        internal inner class CustomerViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var textViewMessage: TextView = itemView.findViewById(R.id.textViewMessage)
            var textViewChatTime: TextView = itemView.findViewById(R.id.textViewChatTime)
        }

        internal inner class EmployeeViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var imageView: ImageView = itemView.findViewById(R.id.imageView)
            var textViewMessage: TextView = itemView.findViewById(R.id.textViewMessage)
            var textViewChatTime: TextView = itemView.findViewById(R.id.textViewChatTime)
        }
    }


    private val mRunnable = object : Runnable {
        override fun run() {
            // Code to reload the RecyclerView
            showDataList()
            if ( currentCount > 0){
                recyclerView!!.adapter!!.notifyDataSetChanged()
            }
            mHandler.postDelayed(this, 2000) // Reload every 2 seconds

        }
    }

    override fun onResume() {
        super.onResume()
        mHandler.postDelayed(mRunnable, 2000)
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(mRunnable)
    }


    private fun postMessage() {
        val url = getString(R.string.root_url) + getString(R.string.chat_post_url)
        val okHttpClient = OkHttpClient()
        val formBody: RequestBody = FormBody.Builder()
            .add("custID", custID.toString())
            .add("empID", empID.toString())
            .add("orderID", orderID.toString())
            .add("message", textViewMessage!!.text.toString())
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
            if (status == "true") {
                showDataList()
                textViewMessage!!.setText("")
            }

        }
    }
}