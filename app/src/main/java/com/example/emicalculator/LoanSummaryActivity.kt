package com.example.emicalculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.TextRange
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

class LoanSummaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_summary)
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val principal=receivePrincipal()
        val interestRate=receiveInterestRate()
        val tenure=receiveTenure()

        val emi=emiCalculate(principal.toDouble(),interestRate.toDouble(),tenure.toInt())
        val emiText=findViewById<TextView>(R.id.emiResultTag)
        emiText.text=indianCurrencyFormat.format(emi).toString()

        val totalLoanAmount=findViewById<TextView>(R.id.loanAmountResult)
        totalLoanAmount.text=indianCurrencyFormat.format(principal.toDouble()).toString()

        val annualRate=findViewById<TextView>(R.id.resultRate)
        annualRate.text=interestRate+"%"

        val totalAmount=totalAmountCalculate(emi,tenure.toInt())
        val totalAmountId=findViewById<TextView>(R.id.totalPaymentResultTag)
        totalAmountId.text=indianCurrencyFormat.format(totalAmount).toString()

        val interestAmount=findViewById<TextView>(R.id.interestResultTag)
        val interest=toCalculateTotalInterest(principal.toDouble(),emi,tenure.toInt())
        interestAmount.text=indianCurrencyFormat.format(interest).toString()

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title="Loan Summary"
    }
    private val indianCurrencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    fun receivePrincipal(): String{
        val bundle: Bundle?=intent.extras
        val principal=bundle?.getString("Loan Amount").toString()
        return principal
    }
    fun receiveInterestRate(): String{
        val bundle: Bundle?=intent.extras
        val rate=bundle?.getString("Interest Rate").toString()
        return rate
    }
    fun receiveTenure(): String{
        //val bundle: Bundle?=intent.extras
        val tenure= intent.extras?.getString("Tenure").toString()
        return tenure
    }
    fun emiCalculate(principal: Double,yearlyRate: Double,n:Int):Double{
        val monthlyRate=(yearlyRate/12)/100
        val emi:Double=((principal*monthlyRate* (1 + monthlyRate).pow(n.toDouble()) /((1 + monthlyRate).pow(
            n.toDouble()) -1)))
        return emi
    }
    fun totalAmountCalculate(emi:Double,tenure: Int): Double{
        val amount=(emi*tenure.toDouble()).toDouble()
        return amount
    }
    fun toCalculateTotalInterest(principal: Double,emi: Double,tenure: Int): Double{
        val amount=(totalAmountCalculate(emi,tenure)-principal).toDouble()
        return amount
    }
}