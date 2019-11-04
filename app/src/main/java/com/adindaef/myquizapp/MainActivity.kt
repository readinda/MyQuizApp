package com.adindaef.myquizapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.TextView
import android.R.id.edit
import android.content.Context
import android.content.SharedPreferences
import com.adindaef.myquizapp.QuizDbHelper.Companion.COLUMN_DIFFICULTY
import com.adindaef.myquizapp.QuizDbHelper.Companion.DIFFICULTY_EASY
import com.adindaef.myquizapp.QuizDbHelper.Companion.DIFFICULTY_HARD
import com.adindaef.myquizapp.QuizDbHelper.Companion.DIFFICULTY_MEDIUM
import com.adindaef.myquizapp.QuizDbHelper.Companion.TABLE_NAME
import android.widget.ArrayAdapter




class MainActivity : AppCompatActivity() {
    companion object{
        var REQUEST_CODE_QUIZ = 1
        val EXTRA_DIFFICULTY = "extraDifficulty"
        val EXTRA_CATEGORY_ID = "extraCategoryId"
        val EXTRA_CATEGORY_NAME = "extraCategoryName"

        var SHARED_PREFS = "sharedPrefs"
        var KEY_HIGHSCORE = "keyHighscore"


        private var highscore: Int = 0

        lateinit var db: QuizDbHelper
        var questionList: ArrayList<Question> = ArrayList<Question>()
        var categoryList: ArrayList<Category> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //difficulty
        val difficultyLevels = Question.getAllDifficultyLevels()
        val adapterDifficulty = ArrayAdapter(this, android.R.layout.simple_spinner_item, difficultyLevels
        )
        adapterDifficulty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_difficulty.setAdapter(adapterDifficulty)

        db = QuizDbHelper(this)


        loadCategory()

        loadHighscore()

        categoryList = db.getAllCategory
        if (categoryList.size < 0){
            fillCategoriesTable()
        }


        button_start_quiz.setOnClickListener {

            questionList = db.getAllQuestions
            if (questionList.size > 0){
                val difficulty = spinner_difficulty.getSelectedItem().toString()

                //categories
                val selectedCategory = spinner_category.getSelectedItem() as Category
                val categoryID = selectedCategory.id
                val categoryName = selectedCategory.name

                val intent = Intent(this@MainActivity, QuizActivity::class.java)
                intent.putExtra(EXTRA_DIFFICULTY, difficulty)
                intent.putExtra(EXTRA_CATEGORY_ID, categoryID)
                intent.putExtra(EXTRA_CATEGORY_NAME, categoryName)
                startActivityForResult(intent, REQUEST_CODE_QUIZ)
            }
            else{
                fillQuestionsTable()
            }
        }
    }

    private fun loadCategory() {
        val dbHelper = QuizDbHelper.getInstance(this)
        val categories = dbHelper.getAllCategory

        val adapterCategories = ArrayAdapter<Category>(
            this,
            android.R.layout.simple_spinner_item, categories
        )
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_category.setAdapter(adapterCategories)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_QUIZ) {
            if (resultCode == Activity.RESULT_OK) {
                val score = data!!.getIntExtra(QuizActivity.EXTRA_SCORE, 0)
                if (score > highscore) {
                    updateHighscore(score)
                }
            }
        }
    }

    private fun loadHighscore() {
        val prefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        highscore = prefs.getInt(KEY_HIGHSCORE, 0)
        text_view_highscore.setText("Highscore: $highscore")
    }

    private fun updateHighscore(highscoreNew: Int) {
        highscore = highscoreNew
        text_view_highscore.setText("Highscore: $highscore")

        val prefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt(KEY_HIGHSCORE, highscore)
        editor.apply()
    }

    fun fillQuestionsTable() {
        val q1 = Question("A is correct", "A", "B", "C", 1, DIFFICULTY_EASY, Category.PROGRAMMING)
        db.addQuestion(q1)
        val q2 = Question("B is correct", "A", "B", "C", 2, DIFFICULTY_HARD, Category.MATH)
        db.addQuestion(q2)
        val q3 = Question("C is correct", "A", "B", "C", 3, DIFFICULTY_MEDIUM, Category.GEOGRAPHY )
        db.addQuestion(q3)
        val q4 = Question("A is correct again", "A", "B", "C", 1, DIFFICULTY_EASY, Category.MATH)
        db.addQuestion(q4)
        val q5 = Question("B is correct again", "A", "B", "C", 2, DIFFICULTY_HARD, Category.PROGRAMMING)
        db.addQuestion(q5)
    }

    fun fillCategoriesTable() {
        val c1 = Category("Programming")
        db.addCategory(c1)
        val c2 = Category("Geography")
        db.addCategory(c2)
        val c3 = Category("Math")
        db.addCategory(c3)
    }
}
