package com.assistant.sol

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.DateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {

    private val speechRecognizer: SpeechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(this)
    }

    private lateinit var outputTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startListeningButton: Button = findViewById(R.id.startListeningButton)
        outputTextView = findViewById(R.id.outputTextView) // TextView for displaying output

        // Permission request logic
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), 1
            )
        }

        startListeningButton.setOnClickListener {
            startListening()
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")

        speechRecognizer.startListening(intent)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                Toast.makeText(applicationContext, "Listening...", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(p0: Float) {}

            override fun onBufferReceived(p0: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                val errorMessage = getErrorText(error)
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.get(0) ?: "No speech detected"
                outputTextView.text = spokenText

                // Handle commands
                processCommand(spokenText.lowercase())
            }

            override fun onPartialResults(p0: Bundle?) {}

            override fun onEvent(p0: Int, p1: Bundle?) {}
        })
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_NETWORK -> "Network error, please try again."
            SpeechRecognizer.ERROR_NO_MATCH -> "I couldn't understand that, Boss."
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
            else -> "Unknown error occurred. Code: $errorCode"
        }
    }

    private fun processCommand(command: String) {
        when (command) {
            "open maps" -> openApp("com.google.android.apps.maps")
            "what's the time" -> showTime()
            else -> Toast.makeText(this, "Command not recognized", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showTime() {
        val currentTime = DateFormat.getTimeInstance().format(Date())
        outputTextView.text = "Current time: $currentTime"
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy() // Release the SpeechRecognizer to avoid memory leaks
    }
}
