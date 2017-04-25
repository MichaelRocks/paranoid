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
    classpath 'io.michaelrocks:paranoid-gradle-plugin:0.1.5'
  }
}

apply plugin: 'com.android.application'
apply plugin: 'io.michaelrocks.paranoid'
```

Now you can just annotate classes with strings that need to be obfuscated with `@Obfuscate`.
After you project compiles every string in annotated classes will be obfuscated.

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

Deobfuscator
------------
The current version of the `Deobfuscator` creates an array of unique characters from all obfuscated
strings and an array of indexes in the character array per each obfuscated string. In the example
above Paranoid generates the following code for the `Deobfuscator`.

```java
public class Deobfuscator {
  private static final char[] chars = new char[] {
    '\u003f', '\u0073', '\u0053', '\u006f', '\u0020', '\u0065', '\u003a', '\u0044',
    '\u0051', '\u0074', '\u0064', '\u0069', '\u006b', '\u0041', '\u0021', '\u0077',
    '\u0072', '\u0025', '\u0075'
  };

  private static final short[][] indexes = new short[][] {
    { 8, 6, 4, 17, 1 },
    { 7, 3, 5, 1, 4, 11, 9, 4, 15, 3, 16, 12, 0 },
    { 13, 6, 4, 17, 1 },
    { 2, 18, 16, 5, 4, 11, 9, 4, 10, 3, 5, 1, 14 },
    { 8, 6, 4, 17, 1 },
    { 13, 6, 4, 17, 1 }
  };

  public static String getString(final int id) {
    final short[] stringIndexes = indexes[id];
    final char[] stringChars = new char[stringIndexes.length];
    for (int i = 0; i < stringIndexes.length; ++i) {
      stringChars[i] = chars[stringIndexes[i]];
    }
    return new String(stringChars);
  }
}
```

License
=======
    Copyright 2016 Michael Rozumyanskiy

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
