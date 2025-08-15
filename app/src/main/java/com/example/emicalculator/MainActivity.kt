package com.example.emicalculator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow
//import com.github.mikephil.charting.data.PieEntry
//import com.github.mikephil.charting.charts.PieChart
//import com.github.mikephil.charting.data.PieData
//import com.github.mikephil.charting.data.PieDataSet
//import com.github.mikephil.charting.utils.ColorTemplate

class MainActivity : AppCompatActivity() {

    private lateinit var etLoanAmount: EditText
    private lateinit var etInterestRate: EditText
    private lateinit var etLoanTerms: EditText
    private lateinit var layoutLoanAmount: TextInputLayout
    private lateinit var layoutInterestRate: TextInputLayout
    private lateinit var layoutLoanTerm: TextInputLayout
    private lateinit var btnYear: ToggleButton
    private lateinit var btnMonth: ToggleButton
    private var selectedTermType: String = "Year"
    private lateinit var emiText: TextView
    private lateinit var loanAmountText: TextView
    private lateinit var rateText: TextView
    private lateinit var totalInterestText: TextView
    private lateinit var totalPaymentText: TextView
    private lateinit var noOfPayment: TextView
    private lateinit var summaryDetails: LinearLayout
    private lateinit var btnViewDetails: Button

    private val indianCurrencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        summaryDetails = findViewById(R.id.summaryDetails)
        etLoanAmount = findViewById(R.id.etLoanAmount)
        etInterestRate = findViewById(R.id.etInterestRate)
        etLoanTerms = findViewById(R.id.etLoanTenure)
        layoutLoanAmount = findViewById(R.id.layoutLoanAmount)
        layoutInterestRate = findViewById(R.id.layoutInterestRate)
        layoutLoanTerm = findViewById(R.id.layoutLoanTenure)
        btnYear = findViewById(R.id.btnYear)
        btnMonth = findViewById(R.id.btnMonth)
        emiText = findViewById(R.id.emiText)
        loanAmountText = findViewById(R.id.loanAmountText)
        rateText = findViewById(R.id.rateText)
        noOfPayment = findViewById(R.id.noOfPayment)
        totalInterestText = findViewById(R.id.totalInterestText)
        totalPaymentText = findViewById(R.id.totalPaymentText)
        btnViewDetails=findViewById(R.id.btnViewDeatails)

        etLoanAmount.setText("5000000")
        etInterestRate.setText("7.5")
        etLoanTerms.setText("20")

        highlightSelectedButton(btnYear, btnMonth)

        btnYear.setOnClickListener {
            selectedTermType = "Year"
            highlightSelectedButton(btnYear, btnMonth)
            //setPieChart()
            calculateAndDisplay()
        }
        btnMonth.setOnClickListener {
            selectedTermType = "Month"
            highlightSelectedButton(btnMonth, btnYear)
            //setPieChart()
            calculateAndDisplay()
        }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val loanAmountStr = etLoanAmount.text.toString().trim()
                val interestRateStr = etInterestRate.text.toString().trim()
                val tenureStr = etLoanTerms.text.toString().trim()

                if (loanAmountStr.isEmpty() || interestRateStr.isEmpty() || tenureStr.isEmpty()) {
                    summaryDetails.visibility = View.GONE
                    return
                }

                val loanAmount = loanAmountStr.toDouble()
                val interestRate = interestRateStr.toDouble()
                val tenure = tenureStr.toInt()

                if(loanAmount>10000000000.0) {
                    layoutLoanAmount.error = "Enter less than 999Cr"
                    setLimitForLoanAmount(etLoanAmount)
                    summaryDetails.visibility = View.GONE
                    return
                }
                else{
                    layoutLoanAmount.error =null
                }

                if(interestRate>100){
                    layoutInterestRate.error="Enter less than 100%"
                    setLimitForRateId(etInterestRate)
                    summaryDetails.visibility = View.GONE
                    return
                }
                else if(interestRate<=0){
                    layoutInterestRate.error="Enter greater than 0%"
                    setLimitForRateId(etInterestRate)
                    summaryDetails.visibility = View.GONE
                    return
                }
                else{
                    layoutInterestRate.error=null
                }

                if(selectedTermType == "Year" && tenure > 100){
                    layoutLoanTerm.error="Enter less than 100Yr"
                    setLimitForYearTenure(etLoanTerms)
                    summaryDetails.visibility = View.GONE
                    return
                }
                else if(selectedTermType == "Year" && tenure <=0){
                    layoutLoanTerm.error="0Yr is invalid"
                    setLimitForYearTenure(etLoanTerms)
                    summaryDetails.visibility = View.GONE
                    return
                }
                else{
                    layoutLoanTerm.error=null
                }

                if(selectedTermType == "Month" && tenure >1200){
                    layoutLoanTerm.error="invalid"
                    setLimitForMonthTenure(etLoanTerms)
                    summaryDetails.visibility = View.GONE
                    return
                }
                else if(selectedTermType == "Month" && tenure <0){
                    layoutLoanTerm.error="0 Month is invalid"
                    setLimitForMonthTenure(etLoanTerms)
                    summaryDetails.visibility = View.GONE
                    return
                }
                else{
                    layoutLoanTerm.error=null
                }
                if(layoutLoanAmount.error==null && layoutInterestRate.error==null && layoutLoanTerm.error==null) {
                    //setPieChart()
                    calculateAndDisplay()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etLoanAmount.addTextChangedListener(watcher)
        etInterestRate.addTextChangedListener(watcher)
        etLoanTerms.addTextChangedListener(watcher)
        calculateAndDisplay()

        btnViewDetails.setOnClickListener {
            val loanAmountStr = etLoanAmount.text.toString().trim()
            val interestRateStr = etInterestRate.text.toString().trim()
            val tenureStr = etLoanTerms.text.toString().trim()

            val tenure = tenureStr.toIntOrNull()?:0

            val tenureMonths = if (selectedTermType == "Year") tenure * 12 else tenure

            val i= Intent(this, LoanRepaymentActivity::class.java)
            i.putExtra("Loan Amount",loanAmountStr)
            i.putExtra("Tenure",tenureMonths.toString())
            i.putExtra("Interest Rate",interestRateStr)
            startActivity(i)
        }
    }

    private fun calculateAndDisplay() {
        val loanAmountStr = etLoanAmount.text.toString().trim()
        val interestRateStr = etInterestRate.text.toString().trim()
        val tenureStr = etLoanTerms.text.toString().trim()

        if (loanAmountStr.isEmpty() || interestRateStr.isEmpty() || tenureStr.isEmpty()) {
            summaryDetails.visibility = View.GONE
            return
        }

        val loanAmount = loanAmountStr.toDoubleOrNull() ?: return
        val interestRate = interestRateStr.toDoubleOrNull() ?: return
        val tenure = tenureStr.toIntOrNull() ?: return

        if (loanAmount < 0 || loanAmount > 10000000000.0 ||
            interestRate <= 0 || interestRate > 100 ||
            (selectedTermType == "Year" && tenure > 100) ||
            (selectedTermType == "Month" && tenure > 600)
        ) {
            summaryDetails.visibility = View.GONE
            return
        }

        val tenureMonths = if (selectedTermType == "Year") tenure * 12 else tenure
        val emi = emiCalculate(loanAmount, interestRate, tenureMonths)
        val totalInterest = toCalculateTotalInterest(loanAmount, emi, tenureMonths)
        val totalPayment = totalAmountCalculate(emi, tenureMonths)

        emiText.text = "EMI  " + indianCurrencyFormat.format(emi)
        loanAmountText.text = "Loan Amount  " + indianCurrencyFormat.format(loanAmount)
        rateText.text = "Interest Rate  $interestRate%"
        noOfPayment.text = "No Of Payment  $tenureMonths"
        totalInterestText.text = "Total Interest  " + indianCurrencyFormat.format(totalInterest)
        totalPaymentText.text = "Total Payment  " + indianCurrencyFormat.format(totalPayment)

        summaryDetails.visibility = View.VISIBLE
    }

    private fun emiCalculate(principal: Double, yearlyRate: Double, n: Int): Double {
        val monthlyRate = (yearlyRate / 12) / 100
        return (principal * monthlyRate * (1 + monthlyRate).pow(n.toDouble())) /
                ((1 + monthlyRate).pow(n.toDouble()) - 1)
    }

    private fun totalAmountCalculate(emi: Double, tenure: Int): Double {
        return emi * tenure
    }

    private fun toCalculateTotalInterest(principal: Double, emi: Double, tenure: Int): Double {
        return totalAmountCalculate(emi, tenure) - principal
    }

    fun setLimitForLoanAmount(loanAmount: EditText){
        val filters= InputFilter{source: CharSequence, start: Int, end: Int, dest: Spanned, dStart: Int, dEnd: Int->
            Log.d("TAG","Hello ${dest.toString()} Vino ${source.toString()}")
            val newVal=(dest.toString() + source.toString())
            try{
                val input=newVal.toDouble()
                if(input in 0.0 ..10000000000.0) null else ""
            }
            catch (e: NumberFormatException) {
                ""
            }
        }
        loanAmount.filters=arrayOf(filters)
    }


    fun setLimitForRateId(interestRateId: EditText){
        val filters= InputFilter{source: CharSequence, start: Int, end: Int, dest: Spanned, dStart: Int, dEnd: Int->
            //Log.d("TAG","Hello ${dest.toString()} Vino ${source.toString()}")
            val newVal=(dest.toString() + source.toString())
            try{
                val input=newVal.toFloat()
                if(input in 0.0 ..100.0) null else ""
            }
            catch (e: NumberFormatException) {
                ""
            }
        }
        interestRateId.filters=arrayOf(filters)
    }
    fun setLimitForYearTenure(tenure: EditText){
        val filters= InputFilter{source: CharSequence, start: Int, end: Int, dest: Spanned, dStart: Int, dEnd: Int->
            //Log.d("TAG","Hello ${dest.toString()} Vino ${source.toString()}")
            val newVal=(dest.toString() + source.toString())
            try{
                val input=newVal.toInt()
                if(input in 1 ..100) null else ""
            }
            catch (e: NumberFormatException) {
                ""
            }
        }
        tenure.filters=arrayOf(filters)
    }
    fun setLimitForMonthTenure(tenure: EditText){
        val filters= InputFilter{source: CharSequence, start: Int, end: Int, dest: Spanned, dStart: Int, dEnd: Int->
            //Log.d("TAG","Hello ${dest.toString()} Vino ${source.toString()}")
            val newVal=(dest.toString() + source.toString())
            try{
                val input=newVal.toInt()
                if(input in 1 ..1200) null else ""
            }
            catch (e: NumberFormatException) {
                ""
            }
        }
        tenure.filters=arrayOf(filters)
    }

    private fun highlightSelectedButton(selected: Button, other: Button) {
        selected.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_700))
        selected.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        other.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        other.setTextColor(ContextCompat.getColor(this, android.R.color.black))
    }
//    fun setPieChart(){
//        val loanAmountStr = etLoanAmount.text.toString().trim()
//        val interestRateStr = etInterestRate.text.toString().trim()
//        val tenureStr = etLoanTerms.text.toString().trim()
//
//        val loanAmount = loanAmountStr.toDoubleOrNull() ?: return
//        val interestRate = interestRateStr.toDoubleOrNull() ?: return
//        val tenure = tenureStr.toIntOrNull() ?: return
//
//        val tenureMonths = if (selectedTermType == "Year") tenure * 12 else tenure
//        val emi = emiCalculate(loanAmount, interestRate, tenureMonths)
//        val totalInterest = toCalculateTotalInterest(loanAmount, emi, tenureMonths)
//        val totalPayment = totalAmountCalculate(emi, tenureMonths)
//
//        val interestPercentage: Float=((totalInterest/totalPayment)*100.0).toFloat()
//        val principalPercentage: Float=((loanAmount/totalPayment)*100.0).toFloat()
//
//        setContentView(R.layout.simple_chart)
//        val pieChart=findViewById<PieChart>(R.id.pieChart)
//        val xValues= ArrayList<String>()
//        xValues.add("Interest")
//        xValues.add("Principal")
//
//        val piechartentry= ArrayList<PieEntry>()
//        piechartentry.add(PieEntry(interestPercentage,"Interest"))
//        piechartentry.add(PieEntry(principalPercentage,"Principal"))
//
//        val piedatset=PieDataSet(piechartentry,"Break-up of Total Payment")
//        val data=PieData(piedatset)
//        pieChart.data=data
//        pieChart.setBackgroundColor(2)
//    }
}
