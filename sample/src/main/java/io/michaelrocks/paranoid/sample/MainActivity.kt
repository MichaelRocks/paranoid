/*
 * Copyright 2020 Michael Rozumyanskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.michaelrocks.paranoid.sample

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
