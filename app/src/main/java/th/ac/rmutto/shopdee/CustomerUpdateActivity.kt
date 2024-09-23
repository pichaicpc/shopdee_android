package th.ac.rmutto.shopdee

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.commitNow
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

class CustomerUpdateActivity : AppCompatActivity() {

    var imageViewFile: ImageView? = null
    var editTextUsername: EditText? = null
    var editTextPassword: EditText? = null
    var editTextFirstname: EditText? = null
    var editTextLastname: EditText? = null
    var editTextEmail: EditText? = null
    var radioButtonMale: RadioButton? = null
    var radioButtonFemale: RadioButton? = null
    var buttonSubmit: Button? = null

    var file: File? = null
    var floatingActionButton: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_update)

        // Set up the toolbar as the action bar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Change the title color
        toolbar.setTitleTextColor(resources.getColor(R.color.white, theme)) // Set the desired color

        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set the title if necessary
        supportActionBar?.title = "แก้ไขข้อมูลผู้ใช้"

        // Set the custom white back arrow drawable
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)

        //For an synchronous task
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


        val sharedPrefer = getSharedPreferences(
            "appPrefer", Context.MODE_PRIVATE)
        val custID = sharedPrefer?.getString("custIDPref", null)
        val token = sharedPrefer?.getString("tokenPref", null)


        imageViewFile = findViewById(R.id.imageViewFile)
        floatingActionButton = findViewById(R.id.floatingActionButton)
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextFirstname = findViewById(R.id.editTextFirstname)
        editTextLastname = findViewById(R.id.editTextLastname)
        editTextEmail = findViewById(R.id.editTextEmail)
        radioButtonMale = findViewById(R.id.radioButtonMale)
        radioButtonFemale = findViewById(R.id.radioButtonFemale)
        buttonSubmit = findViewById(R.id.buttonSubmit)

        //upload or pick a picture
        val launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val uri = it.data?.data!!
                    val path = RealPathUtil.getRealPath(this, uri)

                    file = File(path.toString())
                    imageViewFile?.setImageURI(uri)
                }
            }

        floatingActionButton?.setOnClickListener {
            ImagePicker.Companion.with(this)
                .crop()
                .cropOval()
                .maxResultSize(480, 480)
                .provider(ImageProvider.BOTH) //Or bothCameraGallery()
                .createIntentFromDialog { launcher.launch(it) }
        }

        //update user profile
        buttonSubmit?.setOnClickListener {
            if(editTextUsername?.text.toString() == ""){
                editTextUsername?.error = "กรุณาระบุชื่อผู้ใช้"
                return@setOnClickListener
            }

            if(editTextFirstname?.text.toString() == ""){
                editTextFirstname?.error = "กรุณาระบุชื่อ"
                return@setOnClickListener
            }

            if(editTextLastname?.text.toString() == ""){
                editTextLastname?.error = "กรุณาระบุนามสกุล"
                return@setOnClickListener
            }

            updateUser(custID!!, token!!)

        }

        viewUser(custID!!, token!!)

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

    private fun viewUser(custID: String, token: String)
    {
        val url: String = getString(R.string.root_url) + getString(R.string.profile_url) + custID
        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        val response = okHttpClient.newCall(request).execute()

        if (response.isSuccessful) {
            val data = JSONObject(response.body!!.string())
            if (data.length() > 0) {
                //get data from API
                val imageFile = data.getString("imageFile")
                var username = data.getString("username")
                var firstName = data.getString("firstName")
                var lastName = data.getString("lastName")
                var email = data.getString("email")

                //handle null value
                if(username.equals("null"))username = ""
                if(firstName.equals("null"))firstName = ""
                if(lastName.equals("null"))lastName = ""
                if(email.equals("null"))email = ""

                //show profile image
                if (!imageFile.equals("null") && !imageFile.equals("")){
                    val image_url = getString(R.string.root_url) +
                            getString(R.string.customer_image_url) + imageFile
                    Picasso.get().load(image_url).into(imageViewFile)
                }

                //show profile data
                editTextUsername?.setText(username)
                editTextFirstname?.setText(firstName)
                editTextLastname?.setText(lastName)
                editTextEmail?.setText(email)

                if(data.getString("gender").equals("0")){
                    radioButtonMale?.isChecked = true
                    radioButtonFemale?.isChecked = false
                }else if(data.getString("gender").equals("1")){
                    radioButtonFemale?.isChecked = true
                    radioButtonMale?.isChecked = false
                }

            }
        }
    }


    private fun updateUser(custID: String, token: String)
    {
        var gender = ""
        if(radioButtonMale?.isChecked==true){
            gender = "0"
        }else if(radioButtonFemale?.isChecked==true){
            gender = "1"
        }

        val url = getString(R.string.root_url) + getString(R.string.customer_update_url) + custID
        val okHttpClient = OkHttpClient()
        var formBody: RequestBody? = null

        if(file!=null){
            formBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("custID",custID)
                .addFormDataPart("username",editTextUsername?.text.toString())
                .addFormDataPart("password",editTextPassword?.text.toString())
                .addFormDataPart("firstName",editTextFirstname?.text.toString())
                .addFormDataPart("lastName",editTextLastname?.text.toString())
                .addFormDataPart("email",editTextEmail?.text.toString())
                .addFormDataPart("gender",gender)
                .addFormDataPart(
                    "imageFile", file?.name.toString(),
                    file!!.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                )
                .build()
        }else{
            formBody = FormBody.Builder()
                .add("custID",custID)
                .add("username",editTextUsername?.text.toString())
                .add("password",editTextPassword?.text.toString())
                .add("firstName",editTextFirstname?.text.toString())
                .add("lastName",editTextLastname?.text.toString())
                .add("email",editTextEmail?.text.toString())
                .add("gender",gender)
                .build()
        }

        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .put(formBody)
            .build()
        val response = okHttpClient.newCall(request).execute()

        if(response.isSuccessful) {
            val obj = JSONObject(response.body!!.string())
            val message = obj["message"].toString()
            val status = obj["status"].toString()

            if (status == "true") {
                Toast.makeText(this, "แก้ไขข้อมูลเรียบร้อยแล้ว", Toast.LENGTH_LONG).show()

                //redirect to profile
                val fragment = CustomerFragment()
                supportFragmentManager.commitNow {
                    replace(R.id.navigation_customer, fragment)
                }
            }
        }
    }
}