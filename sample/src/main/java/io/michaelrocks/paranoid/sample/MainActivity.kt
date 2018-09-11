package io.michaelrocks.paranoid.sample

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val questionTextView = findViewById<TextView>(R.id.questionTextView)
        questionTextView.text = String.format(QUESTION, "How does it work?")

        val answerTextView = findViewById<TextView>(R.id.answerTextView)
        answerTextView.text = String.format(ANSWER, "It's magic! ¯\\_(ツ)_/¯")

        val showDialogButton = findViewById<Button>(R.id.showDialogButton)
        showDialogButton.text = "Show dialog"
        showDialogButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "Button clicked", Toast.LENGTH_SHORT).show()
            AlertDialog.Builder(this@MainActivity)
                    .setTitle("Title")
                    .setMessage("Message 2")
                    .setPositiveButton("Close") { dialog, _ ->
                        Toast.makeText(this@MainActivity, "Dialog dismissed", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .show()
        }
    }

    companion object {
        private const val QUESTION = "Q:\r\n%s"
        private const val ANSWER = "A:\r\n%s"
    }
}
