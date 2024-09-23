package th.ac.rmutto.shopdee

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class CustomerFragment : Fragment() {

    var imageViewFile: ImageView? = null
    var textViewUsername: TextView? = null
    var textViewFirstName: TextView? = null
    var textViewLastName: TextView? = null
    var textViewEmail: TextView? = null
    var textViewGender: TextView? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_customer, container, false)

        //For an synchronous task
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val sharedPrefer = requireContext().getSharedPreferences(
            "appPrefer", Context.MODE_PRIVATE)
        val custID = sharedPrefer?.getString("custIDPref", null)
        val token = sharedPrefer?.getString("tokenPref", null)


        imageViewFile = root.findViewById(R.id.imageViewFile)
        textViewUsername = root.findViewById(R.id.textViewUsername)
        textViewFirstName = root.findViewById(R.id.textViewFirstName)
        textViewLastName = root.findViewById(R.id.textViewLastName)
        textViewEmail = root.findViewById(R.id.textViewEmail)
        textViewGender = root.findViewById(R.id.textViewGender)

        val buttonUpdate = root.findViewById<Button>(R.id.buttonUpdate)
        val buttonLogout = root.findViewById<Button>(R.id.buttonLogout)

        buttonUpdate.setOnClickListener {
            val intent = Intent(context, CustomerUpdateActivity::class.java)
            startActivity(intent)
        }

        buttonLogout.setOnClickListener {
            val editor = sharedPrefer.edit()
            editor.clear() // ทำการลบข้อมูลทั้งหมดจาก preferences
            editor.apply() // ยืนยันการแก้ไข preferences

            Toast.makeText(
                requireContext(), "ออกจากระบบเรียบร้อยแล้ว",
                Toast.LENGTH_LONG
            ).show()

            //return to login page
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
        }

        viewUser(custID!!, token!!)

        return root
    }

    private fun viewUser(custID: String, token: String)
    {
        val url: String = getString(R.string.root_url) + getString(R.string.profile_url) + custID
        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")  // Assuming it's a Bearer token
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            val data = JSONObject(response.body!!.string())
            if (data.length() > 0) {
                val imageFile = data.getString("imageFile")
                var username = data.getString("username")
                var firstName = data.getString("firstName")
                var lastName = data.getString("lastName")
                var email = data.getString("email")
                val gender = data.getString("gender")


                if (!imageFile.equals("null") && !imageFile.equals("")){
                    val image_url = getString(R.string.root_url) +
                            getString(R.string.customer_image_url) + imageFile
                    Picasso.get().load(image_url).into(imageViewFile)
                }

                if(username.equals("null"))username = "-"
                textViewUsername?.text = username

                if(firstName.equals("null"))firstName = "-"
                textViewFirstName?.text = firstName

                if(lastName.equals("null"))lastName = "-"
                textViewLastName?.text = lastName

                if(email.equals("null"))email = "-"
                textViewEmail?.text = email

                if(gender.equals("null")){
                    textViewGender?.text = "-"
                }else if(gender.equals("0")){
                    textViewGender?.text = "ชาย"
                }else if(gender.equals("1")){
                    textViewGender?.text = "หญิง"
                }


            }

        }

    }

}