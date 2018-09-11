package io.michaelrocks.paranoid.processor.subproject

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import io.michaelrocks.paranoid.Obfuscate
import io.michaelrocks.paranoid.processor.subproject.android.AndroidConstants
import io.michaelrocks.paranoid.processor.subproject.java.JavaConstants

@Obfuscate
class MainActivity : Activity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        setContentView(textView)
        textView.text = "Subprojects: ${AndroidConstants.getName()}, ${JavaConstants.getName()}"
    }
}
