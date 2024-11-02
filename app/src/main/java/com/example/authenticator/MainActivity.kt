package com.example.authenticator

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        Log.i(TAG, "onCreate usuário atual: ${auth.currentUser}")

        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)
        val googleSignInButton: Button = findViewById(R.id.googleSignInButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            signInWithEmail(email, password)
        }

        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        auth.createUserWithEmailAndPassword(
            "james.francis.byrnes@example-pet-store.com",
            "123456"
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG, "onCreate: Sucesso - Usuário criado")
            } else {
                Log.i(TAG, "onCreate: Falha ao criar usuário -> ${task.exception}")
            }
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "signInWithEmail: Sucesso")
                    Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.i(TAG, "signInWithEmail: Falha -> ${task.exception}")
                    Toast.makeText(this, "Falha no login: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.i(TAG, "onActivityResult: Google Sign-In bem-sucedido")
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w(TAG, "onActivityResult: Falha no Google Sign-In", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "firebaseAuthWithGoogle: Sucesso - Usuário autenticado")
                    Toast.makeText(this, "Login com Google bem-sucedido!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.i(TAG, "firebaseAuthWithGoogle: Falha na autenticação -> ${task.exception}")
                    Toast.makeText(this, "Falha no login com Google: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
