package com.adindaef.myquizapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.os.Build
import androidx.annotation.RequiresApi
import com.adindaef.myquizapp.QuizContract.QuestionsTable






class QuizDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VER) {
    companion object {
        private val DATABASE_NAME = "quiz.db"
        private val DATABASE_VER = 1

        val TABLE_NAME = "quiz_questions"
        //table column
        val COLUMN_ID = "id"
        val COLUMN_QUESTION = "question"
        val COLUMN_OPTION1 = "option1"
        val COLUMN_OPTION2 = "option2"
        val COLUMN_OPTION3 = "option3"
        val COLUMN_ANSWER_NR = "answer_nr"
        val COLUMN_DIFFICULTY = "difficulty"
        val COLUMN_CATEGORY_ID = "category_id"

        //level
        val DIFFICULTY_EASY = "Easy"
        val DIFFICULTY_MEDIUM = "Medium"
        val DIFFICULTY_HARD = "Hard"

        //category
        val TABLE_CAT = "quiz_categories"
        val ID_CAT = "id_cat"
        val NAME_CAT = "name"

        //Category
        private var instance: QuizDbHelper? = null

        @Synchronized
        fun getInstance(context: Context): QuizDbHelper {
            if (instance == null) {
                instance = QuizDbHelper(context.applicationContext)
            }
            return instance!!
        }

    }

    override fun onCreate(db: SQLiteDatabase?) {

        val CREATE_TABLE_CATEGORY: String =
            ("CREATE TABLE $TABLE_CAT ($ID_CAT INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$NAME_CAT TEXT)")
        db!!.execSQL(CREATE_TABLE_CATEGORY)


        val CREATE_TABLE_QUERY: String =
            ("CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, " +
                    "$COLUMN_QUESTION TEXT, " +
                    "$COLUMN_OPTION1 TEXT, " +
                    "$COLUMN_OPTION2 TEXT, " +
                    "$COLUMN_OPTION3 TEXT, " +
                    "$COLUMN_ANSWER_NR INTEGER," +
                    "$COLUMN_DIFFICULTY TEXT, " +
                    "$COLUMN_CATEGORY_ID INTEGER, " +
                    "FOREIGN KEY( $COLUMN_CATEGORY_ID ) REFERENCES $TABLE_CAT ($ID_CAT) ON DELETE CASCADE)")
        db.execSQL(CREATE_TABLE_QUERY)
        // fillQuestionsTable()
        //fillCategoriesTable()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_CAT") //dihapus dulu datanya
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME") //dihapus dulu datanya
        onCreate(db) //dibikin yang baru
    }

    //category
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

//    private fun fillCategoriesTable() {
//        val c1 = Category("Programming")
//        addCategory(c1)
//        val c2 = Category("Geography")
//        addCategory(c2)
//        val c3 = Category("Math")
//        addCategory(c3)
//    }

    fun addCategory(category: Category) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(NAME_CAT, category.name )
        db.insert(TABLE_CAT, null, cv)
    }


    fun addQuestion(question: Question) {

        val db = this.writableDatabase

        val cv = ContentValues()
        cv.put(COLUMN_QUESTION, question.question)
        cv.put(COLUMN_OPTION1, question.option1)
        cv.put(COLUMN_OPTION2, question.option2)
        cv.put(COLUMN_OPTION3, question.option3)
        cv.put(COLUMN_ANSWER_NR, question.answerNr)
        cv.put(COLUMN_DIFFICULTY, question.difficulty)
        cv.put(COLUMN_CATEGORY_ID, question.categoryID)
        db.insert(TABLE_NAME, null, cv)
        db.close()
    }

    //categories
    val getAllCategory: ArrayList<Category>
        get() {
            val listCategory = ArrayList<Category>()
            val selectQueryCategory = "SELECT * FROM $TABLE_CAT"

            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQueryCategory, null)

            if (cursor.moveToFirst()) {
                do {
                    val category = Category()
                    category.id = cursor.getInt(cursor.getColumnIndex(ID_CAT))
                    category.name = cursor.getString(cursor.getColumnIndex(NAME_CAT))

                    listCategory.add(category)
                } while (cursor.moveToNext())
            }
            db.close()
            return listCategory
        }

    val getAllQuestions: ArrayList<Question>
        get() {
            val listquestion = ArrayList<Question>()
            val selectQuery = "SELECT * FROM $TABLE_NAME"

            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)

            if (cursor.moveToFirst()) {
                do {
                    val question = Question()
                    question.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                    question.question = cursor.getString(cursor.getColumnIndex(COLUMN_QUESTION))
                    question.option1 = cursor.getString(cursor.getColumnIndex(COLUMN_OPTION1))
                    question.option2 = cursor.getString(cursor.getColumnIndex(COLUMN_OPTION2))
                    question.option3 = cursor.getString(cursor.getColumnIndex(COLUMN_OPTION3))
                    question.answerNr = cursor.getInt(cursor.getColumnIndex(COLUMN_ANSWER_NR))
                    question.difficulty = cursor.getString(cursor.getColumnIndex(COLUMN_DIFFICULTY))
                    question.categoryID = cursor.getInt(cursor.getColumnIndex(COLUMN_CATEGORY_ID))

                    listquestion.add(question)
                } while (cursor.moveToNext())
            }
            db.close()
            return listquestion
        }

    //belakangan pas mau masuk difficulty
    fun getQuestions(categoryID: Int, difficulty: String): ArrayList<Question> {
        val questionList = ArrayList<Question>()

//        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_DIFFICULTY = ?"

        val db = this.writableDatabase

//        val selectionArgs = arrayOf(difficulty)
//        val cursor = db.rawQuery(selectQuery, selectionArgs)

        //categories
        val selection = COLUMN_CATEGORY_ID + " = ? " +
                " AND " + COLUMN_DIFFICULTY + " = ? "
        val selectionArgs = arrayOf(categoryID.toString(), difficulty)

        val cursor = db.query(
            TABLE_NAME,
            null,
            selection,
            selectionArgs, null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                val question = Question()
                question.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                question.question = cursor.getString(cursor.getColumnIndex(COLUMN_QUESTION))
                question.option1 = cursor.getString(cursor.getColumnIndex(COLUMN_OPTION1))
                question.option2 = cursor.getString(cursor.getColumnIndex(COLUMN_OPTION2))
                question.option3 = cursor.getString(cursor.getColumnIndex(COLUMN_OPTION3))
                question.answerNr = cursor.getInt(cursor.getColumnIndex(COLUMN_ANSWER_NR))
                question.difficulty = cursor.getString(cursor.getColumnIndex(COLUMN_DIFFICULTY))
                question.categoryID = cursor.getInt(cursor.getColumnIndex(COLUMN_CATEGORY_ID))
                questionList.add(question)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return questionList
    }

//
}