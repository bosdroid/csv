package com.boris.expert.csvmagic.view.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.model.User
import com.boris.expert.csvmagic.singleton.DriveService
import com.boris.expert.csvmagic.singleton.SheetService
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.view.fragments.ScanFragment
import com.boris.expert.csvmagic.view.fragments.ScannerFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.button.MaterialButton
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.math.RoundingMode

class StartAppActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var context:Context
    private lateinit var appSettings: AppSettings
    private lateinit var loginBtn:MaterialButton
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    var mService: Drive? = null
    var sheetService: Sheets? = null
    private lateinit var auth: FirebaseAuth
    private val scopes = mutableListOf<String>()
    private val transport: HttpTransport? = NetHttpTransport()
    private val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = NetHttpTransport()
    private val jacksonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
    private var user: User? = null
    private lateinit var firebaseDatabase: DatabaseReference
    var userCurrentCreditsValue: Float = 0F

    companion object{
        var credential: GoogleAccountCredential? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_app)

        initViews()

    }

    private fun initViews(){
        context = this
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        auth = Firebase.auth
        appSettings = AppSettings(context)
        loginBtn = findViewById(R.id.login_btn)
        loginBtn.setOnClickListener(this)

        scopes.add(DriveScopes.DRIVE_METADATA_READONLY)
        scopes.add(SheetsScopes.SPREADSHEETS_READONLY)
        scopes.add(SheetsScopes.DRIVE)
        scopes.add(SheetsScopes.SPREADSHEETS)

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
//            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
//            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions)

        val acct: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

        if (acct != null) {
            startActivity(Intent(context,MainActivity::class.java)).apply {
                finish()
            }
        }

    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.login_btn->{
                startLogin()
            }
            else->{

            }
        }
    }

    private fun startLogin() {
        val signInIntent = mGoogleSignInClient.signInIntent
        googleLauncher.launch(signInIntent)
    }

    // THIS GOOGLE LAUNCHER WILL HANDLE RESULT
    private var googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .addOnSuccessListener(object : OnSuccessListener<GoogleSignInAccount> {
                        override fun onSuccess(googleSignInAccount: GoogleSignInAccount?) {
                            credential = GoogleAccountCredential.usingOAuth2(
                                context,
                                scopes
                            )
                                .setBackOff(ExponentialBackOff())
                                .setSelectedAccount(googleSignInAccount!!.account)

                            mService = Drive.Builder(
                                transport, jsonFactory, credential
                            ).setHttpRequestInitializer { request ->
                                credential!!.initialize(request)
                                request!!.connectTimeout =
                                    300 * 60000  // 300 minutes connect timeout
                                request.readTimeout =
                                    300 * 60000  // 300 minutes read timeout
                            }
                                .setApplicationName(getString(R.string.app_name))
                                .build()

                            try {
                                sheetService = Sheets.Builder(
                                    httpTransport,
                                    jacksonFactory,
                                    credential
                                )
                                    .setApplicationName(getString(R.string.app_name))
                                    .build()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            DriveService.saveDriveInstance(mService!!)
                            SheetService.saveGoogleSheetInstance(sheetService!!)
                            firebaseAuthWithGoogle(googleSignInAccount.idToken!!)
                            saveUserUpdatedDetail(googleSignInAccount, "new")
                        }
                    }).addOnFailureListener(object : OnFailureListener {
                        override fun onFailure(p0: java.lang.Exception) {
                            BaseActivity.showAlert(context, p0.localizedMessage!!)
                        }

                    })
            }
        }


    private fun saveUserUpdatedDetail(acct: GoogleSignInAccount?, isLastSignUser: String) {
        try {

            // IF PART WILL RUN IF USER LOGGED AND ACCOUNT DETAIL NOT EMPTY
            if (acct != null && acct.displayName.isNullOrEmpty()) {
                startLogin()
            } else if (acct != null) {
                val personName = acct.displayName
                val personGivenName = acct.givenName
                val personFamilyName = acct.familyName
                val personEmail = acct.email
                val personId = acct.id
                val personPhoto: Uri? = acct.photoUrl
                val user = User(
                    personName!!,
                    personGivenName!!,
                    personFamilyName!!,
                    personEmail!!,
                    personId!!,
                    personPhoto!!.toString()
                )
                appSettings.putUser(Constants.user, user)
                Constants.userData = user
                if (isLastSignUser == "new") {
                    appSettings.putBoolean(Constants.isLogin, true)
                }
                }
        } catch (e: Exception) {

        }

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    add50CreditsFree()
                }
            }
    }

    @SuppressLint("HardwareIds")
    private fun add50CreditsFree() {
        BaseActivity.startLoading(context)
        val user = Firebase.auth.currentUser
        if (user != null) {

            val id = user.uid as String
            Constants.firebaseUserId = id
            val email = user.email.toString()
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

            val usedIdReference = firebaseDatabase.child("USERS_DEVICES_EMAILS")
            val params = HashMap<String, Any>()
            params["email"] = email
            params["deviceId"] = deviceId

            usedIdReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        var isFound = false

                        for (item: DataSnapshot in snapshot.children) {
                            if (item.child("deviceId").getValue(String::class.java) == deviceId &&
                                item.child("email").getValue(String::class.java) == email) {
                                isFound = true

                                break
                            }
                        }

                        if (!isFound) {
                            firebaseDatabase.child(Constants.firebaseUserCredits)
                                .child(id).addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {

                                        if (snapshot.hasChildren() && snapshot.hasChild("credits")) {
                                            val previousCredits =
                                                snapshot.child("credits").getValue(String::class.java)
                                            userCurrentCreditsValue =
                                                if (previousCredits!!.isNotEmpty()) {
                                                    previousCredits.toFloat()
                                                } else {
                                                    0F
                                                }
                                        }

                                        val roundedCreditValues =
                                            userCurrentCreditsValue.toBigDecimal()
                                                .setScale(2, RoundingMode.UP)
                                                .toDouble()
                                        val totalCredits = roundedCreditValues + 50
                                        val hashMap = HashMap<String, Any>()

                                        hashMap["credits"] = totalCredits.toString()
                                        firebaseDatabase.child(Constants.firebaseUserCredits)
                                            .child(id)
                                            .updateChildren(hashMap)
                                            .addOnSuccessListener {
                                                usedIdReference.push().setValue(params)
                                                moveNext()
                                            }
                                            .addOnFailureListener {
                                                moveNext()
                                            }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        moveNext()
                                    }

                                })
                        }
                        else {
                            moveNext()
                        }
                    } else {

                        firebaseDatabase.child(Constants.firebaseUserCredits)
                            .child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    if (snapshot.hasChildren() && snapshot.hasChild("credits")) {
                                        val previousCredits =
                                            snapshot.child("credits").getValue(String::class.java)
                                        userCurrentCreditsValue =
                                            if (previousCredits!!.isNotEmpty()) {
                                                previousCredits.toFloat()
                                            } else {
                                                0F
                                            }
                                    }

                                    val roundedCreditValues =
                                        userCurrentCreditsValue.toBigDecimal()
                                            .setScale(2, RoundingMode.UP)
                                            .toDouble()
                                    val totalCredits = roundedCreditValues + 50
                                    val hashMap = HashMap<String, Any>()

                                    hashMap["credits"] = totalCredits.toString()
                                    firebaseDatabase.child(Constants.firebaseUserCredits)
                                        .child(id)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener {
                                            usedIdReference.push().setValue(params)
                                            moveNext()
                                        }
                                        .addOnFailureListener {
                                            moveNext()
                                        }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    moveNext()
                                }

                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    moveNext()
                }

            })
        }
        else{
            moveNext()
        }
    }

    private fun moveNext(){

        Handler(Looper.myLooper()!!).postDelayed({
            BaseActivity.dismiss()
            startActivity(Intent(context,MainActivity::class.java)).apply {
                finish()
            }
        },2000)

    }
}