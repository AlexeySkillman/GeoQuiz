package com.example.geoquiz

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.util.Log // Important if I want use Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import java.sql.Array

// Добавление константы TAG (MainActivity.kt)
private const val TAG = "MainActivity"
private const val KEY_INDEX = "index" // Переменая  для savedInstanceState содержит только имя
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {

    // используете lateinit в объявлениях свойств, чтобы указать компилятору,
    // что вы введете ненулевое значение View перед попыткой использовать содержимое свойства.
    // Затем в функции onCreate(...) вы ищете и назначаете объектам представления соответствующие свойства.
    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var cheatButton: Button
    private lateinit var nextButton: ImageButton
    private lateinit var prevButton: ImageButton
    private lateinit var questionTextView: TextView
    private lateinit var resultTextView: TextView
    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProviders.of(this).get(QuizViewModel::class.java)
    }

    // override fun onPause() {}
    // Нужен на случай если программа на сильно завершит все процессы приложения
    // Нежелательно хранить много данных
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        Log.i(TAG, "onSaveInstanceState")
        savedInstanceState.putInt(KEY_INDEX, quizViewModel.currentIndex)
        savedInstanceState.putSerializable("QuestionAnswers", quizViewModel.questionAnswers) // Сохраняем Array
    //  savedInstanceState.putBoolean("IsCheater", quizViewModel.isCheater)
    }
//    override fun onStop() {}

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  Log.d(TAG, "onCreate(Bundle?) called")
        setContentView(R.layout.activity_main)

        val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        quizViewModel.currentIndex = currentIndex
        val questionAnswers = savedInstanceState?.getStringArray("QuestionAnswers") ?: quizViewModel.questionAnswers
        quizViewModel.questionAnswers = questionAnswers

        // ViewModel

        // Затем в функции onCreate(...) мы ищем и назначаете объектам представления соответствующие свойства.
        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        cheatButton = findViewById(R.id.cheat_button)
        nextButton = findViewById(R.id.next_button)
        prevButton = findViewById(R.id.prev_button)
        questionTextView = findViewById(R.id.question_text_view)
        resultTextView = findViewById(R.id.result_text_view)

        // Проверяем Была ли викторина закончена перед Вынужденым отключением
        var checkQA = 0;
        quizViewModel.questionAnswers.forEach {
            if (it.isNotBlank()){
                checkQA += 1
            }
        }
        if(checkQA == quizViewModel.questionBank.size){
            showResult()
        }
        // !!! Проверяем Была ли викторина закончена перед Вынужденым отключением

        // Вешаем Слушатель
        trueButton.setOnClickListener { view: View ->
            checkAnswer(true)
            updateQuestion()
            quizViewModel.isCheater = false

        }
        falseButton.setOnClickListener { view: View ->
            checkAnswer(false)
            updateQuestion()
            quizViewModel.isCheater = false
        }
        cheatButton.setOnClickListener { view: View ->
            // val intent = Intent(this, CheatActivity::class.java)
            quizViewModel.answerIsShowed = false
            val answerIsTrue = quizViewModel.currentQuestionAnswer // получим Boolean questionBank[currentIndex].answer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)

            // Код результата всегда возвращается родителю, если дочерняя activity была запущена функцией startActivityForResult(...).
            // Если функция setResult(...) не вызывалась, то при нажатии пользователем кнопки «Назад» родитель получит код Activity.RESULT_CANCELED.

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Проверяем возможность Версии SDK
                val options = ActivityOptions.makeClipRevealAnimation(view , 0, 0, view.width, view.height) // Важно чтобы view передовался в слушатель
                startActivityForResult(intent, REQUEST_CODE_CHEAT, options.toBundle())
            } else {
                startActivityForResult(intent,REQUEST_CODE_CHEAT) // Cheater  // REQUEST_CODE_CHEAT = 0
            }
            // startActivity(intent)
        }
        updateQuestion()

        questionTextView.setOnClickListener { view: View -> // На входе currentIndex = 0

            quizViewModel.moveToNext()

            var questionCheck: Boolean = try {
                val question = quizViewModel.questionBank[quizViewModel.currentIndex]
                true
            } catch (ex: ArrayIndexOutOfBoundsException) {
                // Регистрация сообщения с уровнем регистрации "error" с трассировкой стека исключений
                Log.e(TAG, "Index was out of bounds: ${quizViewModel.currentIndex}", ex)
                quizViewModel.currentIndex = quizViewModel.questionBank.size - 1
                false
            }

            if(questionCheck){
                updateQuestion()
            } else {
                showResult()
            }
        }

        prevButton.setOnClickListener { view: View -> // На входе currentIndex = 0
            if(quizViewModel.currentIndex != 0) {
                quizViewModel.moveToPrev()
                updateQuestion()
            }
        }

        nextButton.setOnClickListener { view: View -> // На входе currentIndex = 0

            quizViewModel.moveToNext()

            var questionCheck: Boolean = try {
                val question = quizViewModel.questionBank[quizViewModel.currentIndex]
                true
            } catch (ex: ArrayIndexOutOfBoundsException) {
                // Регистрация сообщения с уровнем регистрации "error" с трассировкой стека исключений
                Log.e(TAG, "Index was out of bounds: ${quizViewModel.currentIndex}", ex)
                quizViewModel.currentIndex = quizViewModel.questionBank.size - 1
                false
            }

            Log.d(TAG, "setOnClickListener ___xx___ Check index: $questionCheck")

            if(questionCheck){
                updateQuestion()
            } else {
                showResult()
            }
        }

        updateQuestion()
    }

    override fun onActivityResult (requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode,resultCode, data)

        if (resultCode != Activity.RESULT_OK) { return }
        if (requestCode == REQUEST_CODE_CHEAT) {
            quizViewModel.isCheater = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
        }
    }

    private fun updateQuestion() { // ничего не нужно обьявлять в скобках и передавть так как функция MainActivity содержит нужные переменые

        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)

        showAndHideButtonAnswers()
    }

    private fun checkAnswer(userAnswer: Boolean){

        val correctAnswer = quizViewModel.currentQuestionAnswer

//        val messageResId = if (userAnswer == correctAnswer)
//        { R.string.correct_toast }
//        else { R.string.incorrect_toast }

        val messageResId = when {
            quizViewModel.isCheater -> R.string.judgment_toast
            userAnswer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }

        quizViewModel.questionAnswers[quizViewModel.currentIndex] = getString(messageResId)

        // Log.d(TAG, "checkAnswer ___________ check index: ${quizViewModel.questionAnswers[quizViewModel.currentIndex]}")

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT) .show()

        showAndHideButtonAnswers()
    }

    private fun showAndHideButtonAnswers(){
        if(!quizViewModel.questionAnswers[quizViewModel.currentIndex].isBlank()){
            trueButton.setVisibility(View.INVISIBLE);
            falseButton.setVisibility(View.INVISIBLE);
        } else {
            trueButton.setVisibility(View.VISIBLE);
            falseButton.setVisibility(View.VISIBLE);
        }
    }
    private fun showResult(){
        var stringTextR = ""
        quizViewModel.questionBank.forEachIndexed { index, element ->
            var getTextQ = getString( quizViewModel.questionBank[index].textResId )
            var getTextA = quizViewModel.questionAnswers[index]
            stringTextR += "$getTextQ - $getTextA \n \n"
        }
        resultTextView.setVisibility(View.VISIBLE)
        resultTextView.setText(stringTextR)

        val balOneQ = 100 / quizViewModel.questionAnswers.size
        var balResult = 0

        quizViewModel.questionAnswers.forEach {
            if (it == getString(R.string.correct_toast)){
                balResult += balOneQ
            }
        }

        val toast = Toast.makeText(this, "You right in $balResult % questions", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
        toast.show()

        questionTextView.setVisibility(View.INVISIBLE)
        nextButton.setVisibility(View.INVISIBLE)
        prevButton.setVisibility(View.INVISIBLE)
        trueButton.setVisibility(View.INVISIBLE);
        falseButton.setVisibility(View.INVISIBLE);
    }
}