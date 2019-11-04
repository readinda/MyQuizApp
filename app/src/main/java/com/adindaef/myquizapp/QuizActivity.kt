package com.adindaef.myquizapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.ListAdapter
import kotlinx.android.synthetic.main.activity_quiz.*
import java.util.*
import kotlin.collections.ArrayList
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.get
import android.os.CountDownTimer
import android.R.string.cancel
import android.annotation.SuppressLint


class QuizActivity : AppCompatActivity() {
    companion object{
        var EXTRA_SCORE = "extraScore"
        val COUNTDOWN_IN_MILLIS: Long = 30000
        val KEY_SCORE = "keyScore"
        val KEY_QUESTION_COUNT = "keyQuestionCount"
        val KEY_MILLIS_LEFT = "keyMillisLeft"
        val KEY_ANSWERED = "keyAnswered"
        val KEY_QUESTION_LIST = "keyQuestionList"

    }

    lateinit var db: QuizDbHelper
    var questionList: ArrayList<Question> = ArrayList<Question>()
    var textColorDefaultRb: ColorStateList? = null
    var textColorDefaultCd: ColorStateList? = null

    var countDownTimer: CountDownTimer? = null
    var timeLeftInMillis: Long = 0

    var questionCounter: Int = 0
    var questionCountTotal: Int = 0
    var currentQuestion: Question? = null

    var score: Int = 0
    var answered: Boolean = false
    private var backPressedTime: Long = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        textColorDefaultRb = rb1.getTextColors()
        textColorDefaultCd = text_view_countdown.getTextColors()

        //difficulty
        val intent = intent
        val difficulty = intent.getStringExtra(MainActivity.EXTRA_DIFFICULTY)
        val categoryID = intent.getIntExtra(MainActivity.EXTRA_CATEGORY_ID, 0)
        val categoryName = intent.getStringExtra(MainActivity.EXTRA_CATEGORY_NAME)

        textViewDifficulty.setText("Difficulty: " + difficulty);
        textViewCategory.setText("Category: " + categoryName);


        if (savedInstanceState == null) {
//            db = QuizDbHelper(this)
            db = QuizDbHelper.getInstance(this)
//            questionList = db.getAllQuestions
            questionList = db.getQuestions(categoryID, difficulty)

            questionCountTotal = questionList.size
            Collections.shuffle(questionList)
            showNextQuestion();
        } else {
            questionList = savedInstanceState.getParcelableArrayList(KEY_QUESTION_LIST)!!
//            if (questionList == null){
//                finish()
//            }
            questionCountTotal = questionList.size
            questionCounter = savedInstanceState.getInt(KEY_QUESTION_COUNT)
            currentQuestion = questionList.get(questionCounter - 1)
            score = savedInstanceState.getInt(KEY_SCORE)
            timeLeftInMillis = savedInstanceState.getLong(KEY_MILLIS_LEFT)
            answered = savedInstanceState.getBoolean(KEY_ANSWERED)

            if (!answered) {
                startCountDown();
            } else {
                updateCountDownText();
                showSolution();
            }
        }

        buttonConfirmNext.setOnClickListener {

            if (timeLeftInMillis > 0) {
                if (!answered) {
                    if (rb1.isChecked || rb2.isChecked || rb3.isChecked) {
                        checkAnswer()
                    } else {
                        Toast.makeText(
                            this@QuizActivity,
                            "Please select an answer",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    showNextQuestion()
                }
            } else{
                showNextQuestion()
            }

        }

    }

    private fun showNextQuestion() {
        rb1.setTextColor(textColorDefaultRb);
        rb2.setTextColor(textColorDefaultRb);
        rb3.setTextColor(textColorDefaultRb);
        rbGroup.clearCheck();

        if (questionCounter < questionCountTotal) {
            currentQuestion = questionList.get(questionCounter);

            textViewQuestion.setText(currentQuestion!!.question)
            rb1.setText(currentQuestion!!.option1);
            rb2.setText(currentQuestion!!.option2);
            rb3.setText(currentQuestion!!.option3);

            questionCounter++;
            textViewQuestionCount.setText("Question: " + questionCounter + "/" + questionCountTotal);
            answered = false;
            buttonConfirmNext.setText("Confirm");

            timeLeftInMillis = COUNTDOWN_IN_MILLIS;
            startCountDown();
        } else {
            finishQuiz()
        }

    }

    private fun startCountDown() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                updateCountDownText()

                    if (rb1.isChecked || rb2.isChecked || rb3.isChecked) {
                        checkAnswer()
                    } else {
                        showSolution()
                    }

            }
        }.start()
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60

        val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        text_view_countdown.setText(timeFormatted)

        if (timeLeftInMillis < 10000) {
            text_view_countdown.setTextColor(Color.RED)
        } else {
            text_view_countdown.setTextColor(textColorDefaultCd)
        }
    }


    private fun checkAnswer() {
        answered = true

        countDownTimer!!.cancel()


        var rbSelected: RadioButton = findViewById(rbGroup.checkedRadioButtonId)
        val answerNr = rbGroup.indexOfChild(rbSelected) + 1


        if (answerNr == currentQuestion!!.answerNr) {
            score++
            textViewScore.setText("Score: $score")
        }

        showSolution()
    }

    private fun showSolution() {
        rb1.setTextColor(Color.RED)
        rb2.setTextColor(Color.RED)
        rb3.setTextColor(Color.RED)

        when (currentQuestion!!.answerNr) {
            1 -> {
                rb1.setTextColor(Color.GREEN)
                textViewQuestion.text = "Answer 1 is correct"
            }
            2 -> {
                rb2.setTextColor(Color.GREEN)
                textViewQuestion.text = "Answer 2 is correct"
            }
            3 -> {
                rb3.setTextColor(Color.GREEN)
                textViewQuestion.text = "Answer 3 is correct"
            }
        }

        if (questionCounter < questionCountTotal) {
            buttonConfirmNext.text = "Next"
        } else {
            buttonConfirmNext.text = "Finish"
        }
    }

    private fun finishQuiz() {

        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_SCORE, score)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onBackPressed() {
        //db.deleteTable()
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            finishQuiz()
        } else {
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show()
        }

        backPressedTime = System.currentTimeMillis()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
        }
    }

    //biar datanya tersimpan ketika device ke rotate
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SCORE, score)
        outState.putInt(KEY_QUESTION_COUNT, questionCounter)
        outState.putLong(KEY_MILLIS_LEFT, timeLeftInMillis)
        outState.putBoolean(KEY_ANSWERED, answered)
        outState.putParcelableArrayList(KEY_QUESTION_LIST, questionList)
    }


}
