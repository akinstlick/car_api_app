package com.example.mobile_final_project

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class MainActivity : AppCompatActivity() {

    var carMake = ""
    var carYear = ""
    var resultView: ListView? = null
    var carResults: ArrayList<String> = ArrayList()
    var enterCarMake: EditText? = null
    var enterCarYear: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var buttonView: Button = findViewById(R.id.button)
        enterCarMake = findViewById(R.id.enterCarMake)
        enterCarYear = findViewById(R.id.enterCarYear)
        buttonView.setOnClickListener {
            GlobalScope.async {
                carMake = enterCarMake?.text.toString()
                carYear = enterCarYear?.text.toString()
                //do an if statement for year
                if(carYear == ""){
                    getModels(buttonView)
                } else {
                    getModelsWithYear(buttonView)
                }
            }
        }
        resultView = findViewById(R.id.resultView)
    }

    private fun updateListView() {
        runOnUiThread {
            resultView?.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,carResults)
        }
    }


    public suspend fun getModels(view: android.view.View) {
        try {
            val result = GlobalScope.async {
                callCarAPI("https://vpic.nhtsa.dot.gov/api/vehicles/getmodelsformake/" + carMake.lowercase() + "?format=json")
            }.await()

            onResponse(result)


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public suspend fun getModelsWithYear(view: View){
        try {
            val result = GlobalScope.async {
                callCarAPI("https://vpic.nhtsa.dot.gov/api/vehicles/getmodelsformakeyear/make/" + carMake.lowercase() + "/modelyear/" + carYear.lowercase() + "?format=json")
            }.await()

            onResponse(result)


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun callCarAPI(apiUrl:String ):String?{
        var result: String? = ""
        val url: URL;
        var connection: HttpsURLConnection? = null
        try {
            url = URL(apiUrl)
            connection = url.openConnection() as HttpsURLConnection
            // set the request method - POST
            connection.requestMethod = "GET"
            val `in` = connection.inputStream
            val reader = InputStreamReader(`in`)

            // read the response data
            var data = reader.read()
            while (data != -1) {
                val current = data.toChar()
                result += current
                data = reader.read()
            }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // if not able to retrieve data return null
        return null

    }

    private fun onResponse(result: String?) {
        carResults = ArrayList()
        try {
            // convert the string to JSON object for better reading
            val resultJson = JSONObject(result)
            Log.i("RESULT FOUND! Length:","${resultJson.toString()}")
            // Update text with various fields from response
            val resultarr = resultJson.getJSONArray("Results")
            if(resultarr.length() == 0){
                carResults = ArrayList()
                carResults.add("No models found :(")
                updateListView()
            } else {
                for (i in 0 until resultarr.length()){
                    val resultjson: JSONObject = resultarr[i] as JSONObject
                    carResults.add(resultjson.get("Model_Name") as String)
                }
            }
            carResults.sort()
            //Update the prediction to the view
            //Probably want to do my list view stuff here
            updateListView()
        } catch (e: Exception) {
            carResults = ArrayList()
            carResults.add("Error in make name: Please format correctly")
            updateListView()
            e.printStackTrace()
        }
    }

    private fun setText(text: TextView?, value: String) {
        runOnUiThread { text!!.text = value }
    }
}