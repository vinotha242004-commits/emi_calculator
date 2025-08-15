package com.example.emicalculator

import android.R.attr.bitmap
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlin.math.pow
import android.util.Log
import android.app.Application
import android.content.ComponentCallbacks

class EmiPaymentAnalysis : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Tag","Principal ${receivePrincipal()} Interest ${receiveInterestRate()} Tenure ${receiveTenure()}")
        setContentView(R.layout.activity_emi_payment_analysis)
        setPieChart()
    }

//    override fun onTrimMemory(level: Int) {
//        super.onTrimMemory(level)
//        if(level>= ComponentCallbacks2.TRIM_MEMORY_MODERATE)
//    }
    fun setPieChart(){
//        val loanAmountStr = etLoanAmount.text.toString().trim()
//        val interestRateStr = etInterestRate.text.toString().trim()
//        val tenureStr = etLoanTerms.text.toString().trim()
//
//        val loanAmount = loanAmountStr.toDoubleOrNull() ?: return
//        val interestRate = interestRateStr.toDoubleOrNull() ?: return
//        val tenure = tenureStr.toIntOrNull() ?: return
        Log.d("Tag","Principal ${receivePrincipal()} Interest ${receiveInterestRate()} Tenure ${receiveTenure()}")
        val loanAmount=receivePrincipal().toDoubleOrNull()?: return
        val interestRate=receiveInterestRate().toDoubleOrNull()?: return
        val tenure=receiveTenure().toIntOrNull()?: return

        val emi = emiCalculate(loanAmount, interestRate, tenure)
        val totalInterest = toCalculateTotalInterest(loanAmount, emi, tenure)
        val totalPayment = totalAmountCalculate(emi, tenure)

        val interestPercentage: Float=((totalInterest/totalPayment)*100.0).toFloat()
        val principalPercentage: Float=((loanAmount/totalPayment)*100.0).toFloat()
        val pieChart=findViewById<PieChart>(R.id.pieChart)
        val xValues= ArrayList<String>()
        xValues.add("Interest")
        xValues.add("Principal")

        val piechartentry= ArrayList<PieEntry>()
        piechartentry.add(PieEntry(interestPercentage,"Interest"))
        piechartentry.add(PieEntry(principalPercentage,"Principal"))

        val piedatset=PieDataSet(piechartentry,"Break-up of Total Payment")
        val data=PieData(piedatset)
        pieChart.data=data
        pieChart.setBackgroundColor(2)
    }
    fun receivePrincipal(): String{
        val bundle: Bundle?=intent.extras
        val principal=bundle?.getString("Loan Amount").toString().trim()
        return principal
    }
    fun receiveInterestRate(): String{
        val bundle: Bundle?=intent.extras
        val rate=bundle?.getString("Interest Rate").toString().trim()
        return rate
    }
    fun receiveTenure():String{
        val bundle:Bundle?=intent.extras
        val tenure=bundle?.getString("Tenure").toString().trim()
        return tenure
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
}