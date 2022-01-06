package com.example.geoquiz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders

private const val TAG = "CheatActivity"

const val EXTRA_ANSWER_SHOWN = "com.example.android.geoquiz.answer_shown" // Имя с префиксом пакета нужно передовать по правилам
private const val EXTRA_ANSWER_IS_TRUE ="com.example.android.geoquiz.answer_is_true" // Имя с префиксом пакета нужно передовать по правилам

class CheatActivity : AppCompatActivity() {

    private lateinit var versionTextView: TextView
    private lateinit var answerTextView: TextView
    private lateinit var showAnswerButton: Button
    private var answerIsTrue = false
    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProviders.of(this).get(QuizViewModel::class.java)
    }

    // Защита от потери
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putBoolean("IsCheater", quizViewModel.answerIsShowed)
    }
    // Защита от потери

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cheat)

        versionTextView  = findViewById(R.id.version_text_view)
        answerIsTrue     = intent.getBooleanExtra(EXTRA_ANSWER_IS_TRUE,false) // EXTRA_ANSWER_IS_TRUE - com.example.android.geoquiz.answer_is_true
        answerTextView   = findViewById(R.id.answer_text_view)
        showAnswerButton = findViewById(R.id.show_answer_button)

        versionTextView.append("SDK / API LEVEL: ${Build.VERSION.SDK_INT}")

        // Защита от потери
        val isCheater = savedInstanceState?.getBoolean("IsCheater",  false) ?: false
        quizViewModel.answerIsShowed = isCheater
        //  Log.i(TAG, "isCheater =  $isCheater")

        if(quizViewModel.answerIsShowed){
            setAnswerShownResult(true)
        }
        // Защита от потери

        showAnswerButton.setOnClickListener {
            val answerText = when {
                answerIsTrue ->
                    R.string.true_button
                else -> R.string.false_button
            }
            answerTextView.setText(answerText)
            setAnswerShownResult(true)
            quizViewModel.answerIsShowed = true
        }

    }

    private fun setAnswerShownResult(isAnswerShown: Boolean) { // Был ли показан ответ
        val data = Intent().apply {
            putExtra(EXTRA_ANSWER_SHOWN, isAnswerShown) // EXTRA_ANSWER_SHOWN - com.example.android.geoquiz.answer_shown
        }
        // Существует две функции, которые могут вызываться в дочерней activity для возвращения данных родителю:
        // setResult(resultCode: Int)
        // setResult(resultCode: Int, data: Intent)
        // (Также можно использовать другую константу, RESULT_FIRST_USER, как смещение при определении собственных кодов результатов.)

        // Log.i(TAG, "setAnswerShownResult: $isAnswerShown")
        setResult(Activity.RESULT_OK, data)
    }

    companion object {
        fun newIntent(packageContext: Context, answerIsTrue: Boolean): Intent {
            return Intent(packageContext, CheatActivity::class.java)
                .apply { putExtra(EXTRA_ANSWER_IS_TRUE, answerIsTrue) } // EXTRA_ANSWER_IS_TRUE - com.example.android.geoquiz.answer_is_true
        }
    }

}