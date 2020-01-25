[![Build Status](https://travis-ci.org/MichaelRocks/paranoid.svg?branch=master)](https://travis-ci.org/MichaelRocks/paranoid)

Paranoid
========

String obfuscator for Android applications.

Usage
-----
In order to make Paranoid work with your project you have to apply the Paranoid Gradle plugin
to the project. Please notice that the Paranoid plugin must be applied **after** the Android
plugin.

```groovy
buildscript {
  repositories {
    jcenter()
  }

  dependencies {
    classpath 'io.michaelrocks:paranoid-gradle-plugin:0.3.0'
  }
}

apply plugin: 'com.android.application'
apply plugin: 'io.michaelrocks.paranoid'
```

Now you can just annotate classes with strings that need to be obfuscated with `@Obfuscate`.
After you project compiles every string in annotated classes will be obfuscated.

Configuration
-------------
Paranoid plugin can be configured using `paranoid` extension object:
```groovy
paranoid {
  // ...
}

```

The extension object contains the following properties:
- `enabled` — `boolean`. Allows to disable obfuscation for the project. Default value is `true`.  
- `includeSubprojects` — `boolean`. Allows to enable obfuscation for subprojects. Default value is `false`.  

How it works
------------
Let's say you have an `Activity` that contains some string you want to be obfuscated.

```java
@Obfuscate
public class MainActivity extends AppCompatActivity {
  private static final String QUESTION = "Q: %s";
  private static final String ANSWER = "A: %s";

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);

    final TextView questionTextView = (TextView) findViewById(R.id.questionTextView);
    questionTextView.setText(String.format(QUESTION, "Does it work?"));

    final TextView answerTextView = (TextView) findViewById(R.id.answerTextView);
    answerTextView.setText(String.format(ANSWER, "Sure it does!"));
  }
}
```

The class contains both string constants (`QUESTION` and `ANSWER`) and string literals.
After compilation this class will be transformed to something like this.

```java
@Obfuscate
public class MainActivity extends AppCompatActivity {
  private static final String QUESTION = Deobfuscator.getString(4);
  private static final String ANSWER = Deobfuscator.getString(5);

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);

    TextView questionTextView = (TextView) findViewById(R.id.questionTextView);
    questionTextView.setText(String.format(Deobfuscator.getString(0), Deobfuscator.getString(1)));

    TextView answerTextView = (TextView) findViewById(R.id.answerTextView);
    answerTextView.setText(String.format(Deobfuscator.getString(2), Deobfuscator.getString(3)));
  }
}

```

License
=======
    Copyright 2020 Michael Rozumyanskiy

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
