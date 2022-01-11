package com.example.datatransfer

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.RequestParams
import com.loopj.android.http.TextHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    private lateinit var user_list:  ArrayList<User>
    private var count = 0
    private var user_count = -1
    private lateinit var pd:ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        user_list = ArrayList()

        pd = ProgressDialog(this)
        pd.setTitle("Please wait")
        pd.setMessage("Sending emails")

        send_emails.setOnClickListener{
            FirebaseFirestore.getInstance().collection("users").get().addOnSuccessListener {
                user_count = it.documents.size
                for(i in it.documents){
                    var username = i.get("username").toString()
                    var email = i.get("email").toString()
                    var phone_number = ""
                    try{
                        phone_number = i.get("mobile_number").toString()
                    }
                    catch(e:Exception){

                    }
                    var logged_in = "false"
                    var date_registered = System.currentTimeMillis().toString()
                    var expiry_date = i.get("expiry_date").toString()
                    var firebase_tokens = ""
                    var user_password = i.get("password").toString()
                    var active = "true"
                    var app_version_ios = ""
                    var app_version_android = ""
                    var device = ""
                    var ip_address = ""
                    var profile_image_url = ""
                    var hash = hash(email.toString().lowercase())

                    var user = User(
                        username= username,
                        email=email,
                        phone_number=phone_number,
                        logged_in= logged_in,
                        date_registered = date_registered,
                        expiry_date = expiry_date,
                        firebase_tokens = firebase_tokens,
                        user_password = user_password,
                        active = active,
                        app_version_ios = app_version_ios,
                        app_version_android = app_version_android,
                        device = device,
                        ip_address = ip_address,
                        profile_image_url = profile_image_url,
                        hash = hash
                    )
                    user_list.add(user)
                }
                sendEmail()
                println("Total emails sent to ${count.toString()} of ${user_list.size}")
            }
        }
    }

    fun hash(value: String): String {
        val bytes = value.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    fun sendEmail(){
        for(user in user_list){
            println("Sending email to ${user.email}")
            val ios_app_link =
                "https://apps.apple.com/us/app/selectivetradesapp/id1593216934#?platform=iphone"
            val android_app_link =
                "https://play.google.com/store/apps/details?id=com.selectivetradesapp"
            var message = "Hello\n" +
                    "\n" +
                    "Please cancel your old subscription if you are a valid member from app store subscriptions so that it will not charge any more. \n" +
                    "\n" +
                    "any further help contact support@selectivetrades.com"
            var http = AsyncHttpClient()
            var params = RequestParams()
            params.put("pin", message)
            params.put("email", user.email)
            http.get("https://www.hnitrade.com/SelectiveTradesApp/sendPinToEmail.php", params, object: TextHttpResponseHandler(){
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    responseString: String?
                ) {
                    println("Email sent to ${user.email} successfully")
                    count += 1
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    responseString: String?,
                    throwable: Throwable?
                ) {
                    println("Failed to send email to ${user.email}")
                }
            })
            Thread.sleep(7_000)
        }
    }

    fun uploadUser(){
        var query = "insert into users (username, email, phone_number, date_registered, expiry_date," +
                " firebase_tokens, user_password, logged_in, active, app_version_ios," +
                " app_version_android, device, ip_address, profile_image_url, hash) values "
        var http = AsyncHttpClient()
        for(user in user_list){
            var to_insert = "('${user.username}', '${user.email}', '${user.phone_number}', '${user.date_registered}', '${user.expiry_date}', " +
                    "'${user.firebase_tokens}', '${user.user_password}', '${user.logged_in}', '${user.active}', '${user.app_version_ios}', '${user.app_version_android}', " +
                    "'${user.device}', '${user.ip_address}', '${user.profile_image_url}', '${user.hash}')"
            var user_iterator = user_list.iterator()
            if(!user_iterator.hasNext()){
                query = query + to_insert+"\n"
            }
            else{
                query = query + to_insert+",\n"
            }
            var params = RequestParams()
            params.put("query", query)
        }

        var params = RequestParams()
        var q = query.substring(0, query.length-2)
        println("MainActivity. query = ${q.substring(query.length-10)}")
        params.put("query", q)
        http.post("http://www.hnitrade.com/SelectiveTradesApp/writeUsers.php", params, object:
            TextHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseString: String?
            ) {
                println("MainActivity.uploadUser onSuccess: Users uploaded ")
                count += 1
                Thread.sleep(2_000)
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseString: String?,
                throwable: Throwable?
            ) {
                println("MainActivity.uploadUser onFailure: User}")
            }
        })
    }
}