package com.example.propelrrform

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.regex.Pattern
import kotlinx.coroutines.*
import java.net.URLEncoder


class MainActivity : AppCompatActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var mobileNumberEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var datePicker: DatePicker
    private lateinit var ageTextview: TextView
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fullNameEditText = findViewById(R.id.name)
        emailEditText = findViewById(R.id.email)
        mobileNumberEditText = findViewById(R.id.mobile_number)
        genderSpinner = findViewById(R.id.spinner)
        datePicker = findViewById(R.id.date_picker)
        ageTextview = findViewById(R.id.age_textview)
        submitButton = findViewById(R.id.submit)

        datePicker.init(
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, dayOfMonth ->
            // Calculate age based on selected date
            val selectedDate = Calendar.getInstance().apply {
                set(year, monthOfYear, dayOfMonth)
            }
            val age = calculateAge(selectedDate, Calendar.getInstance())

            // Update TextView with the calculated age
            "  Age: $age years old".also { ageTextview.text = it }
        }

        submitButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val mobileNumber = mobileNumberEditText.text.toString()
            val ageString = ageTextview.text.toString()

            try {
                // Extract numeric part from the string
                val age = ageString.filter { it.isDigit() }.toInt()

                if (isValidFullName(fullName) && isValidEmail(email) && isValidMobileNumber(mobileNumber) && isAgeValid(age)) {
                    // All validations passed, you can proceed with your logic here
                    Toast.makeText(this, "All fields are valid", Toast.LENGTH_SHORT).show()
                    FormSubmitTask(this@MainActivity).execute(fullName, email, mobileNumber, age.toString())
                } else {
                    // Invalid input, show an error message
                    Toast.makeText(this, "Invalid input. Please check the fields.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: NumberFormatException) {
                // Handle the case where the extracted age is not a valid integer
                Toast.makeText(this, "Invalid age selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidFullName(fullName: String): Boolean {
        val regex = "^[a-zA-Z ,.']+\$"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(fullName)
        return matcher.matches()
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidMobileNumber(mobileNumber: String): Boolean {
        val regex = "^(09|\\+639)\\d{9}\$"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(mobileNumber)
        return matcher.matches()
    }

    private fun isAgeValid(age: Int): Boolean {
        return age >= 18
    }

    private fun extractNumericPart(input: String): String {
        return input.replace(Regex("[^0-9]"), "")
    }

    private fun calculateAge(selectedDate: Calendar, currentDate: Calendar): Int {
        var age = currentDate.get(Calendar.YEAR) - selectedDate.get(Calendar.YEAR)

        // Adjust age if the birthday hasn't occurred yet this year
        if (currentDate.get(Calendar.DAY_OF_YEAR) < selectedDate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return age
    }

    @SuppressLint("StaticFieldLeak")
    private inner class FormSubmitTask(context: Context) : AsyncTask<String, Void, String>() {

        private val progressDialog = AlertDialog.Builder(context).create()

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog.setMessage("Submitting Form...")
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        override fun doInBackground(vararg params: String): String {
            val fullName = URLEncoder.encode(params[0], "UTF-8")
            val email = URLEncoder.encode(params[1], "UTF-8")
            val mobileNumber = URLEncoder.encode(params[2], "UTF-8")
            val age = URLEncoder.encode(params[3], "UTF-8")

            // API integration using www.mocky.io
            val url = "https://run.mocky.io/v3/31d4331e-eafc-49e6-8c6b-7eefa12252f4?fullName=$fullName&email=$email&mobileNumber=$mobileNumber&age=$age"
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                return bufferedReader.readText()
            } finally {
                connection.disconnect()
            }
        }


        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            progressDialog.dismiss()

            // Display the API response using a dialog
            showResponseDialog(result)
        }
    }

    private fun showResponseDialog(response: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("API Response")
        alertDialogBuilder.setMessage(response)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
