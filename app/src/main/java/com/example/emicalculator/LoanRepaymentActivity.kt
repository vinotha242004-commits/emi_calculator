package com.example.emicalculator


import android.content.Intent
import android.graphics.Typeface
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.AdapterView
import android.widget.Button
import android.widget.LinearLayout
import java.security.Principal
import java.text.NumberFormat
import java.util.Locale

class LoanRepaymentActivity : AppCompatActivity() {

    private lateinit var emiTable: TableLayout
    private lateinit var monthSpinner: Spinner
    private lateinit var yearSpinner: Spinner
    private lateinit var yearData: List<EMIYearSummary>

    private lateinit var btnGraph: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_repayment)

        emiTable = findViewById(R.id.emiTable)
        monthSpinner=findViewById<Spinner>(R.id.monthSpinner)
        yearSpinner=findViewById<Spinner>(R.id.yearSpinner)
        btnGraph=findViewById<Button>(R.id.btnGraph)

        val months=listOf<String>("Jan","Feb","Mar","Apr","May","June","July","Aug","Sep","Oct","Nov","Dec")
        monthSpinner.adapter= ArrayAdapter(this,android.R.layout.simple_list_item_1,months)
        val years=(1950..2100).toList()
        yearSpinner.adapter= ArrayAdapter(this,android.R.layout.simple_list_item_1,years)

        val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())

        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        Log.d("Tag","Year-${currentYear.toString()}  Month -${currentMonth.toString()}")
        monthSpinner.setSelection(ArrayAdapter(this,android.R.layout.simple_list_item_1,months).getPosition(months.get(currentMonth)))
        yearSpinner.setSelection(ArrayAdapter(this,android.R.layout.simple_list_item_1,years).getPosition(currentYear))
        var selectedMonth=monthSpinner.selectedItemPosition
        var selectedYear=yearSpinner.selectedItem as Int

        val principal=receivePrincipal().toDouble()
        val annualInterestRate=receiveInterestRate().toDouble()
        val tenureMonths=receiveTenure().toInt()

        Log.d("Tag","principal-$principal Interest-$annualInterestRate  Tenure-$tenureMonths")

        yearData = generateRepaymentTable(selectedMonth,selectedYear,principal,annualInterestRate,tenureMonths,months)

        addHeaderRow()
        populateYearRows()

        monthSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedMonth=monthSpinner.selectedItemPosition
                selectedYear=yearSpinner.selectedItem as Int
                refreshTable(selectedMonth,selectedYear,principal,annualInterestRate,tenureMonths,months)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })

        yearSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedMonth=monthSpinner.selectedItemPosition
                selectedYear=yearSpinner.selectedItem as Int
                refreshTable(selectedMonth,selectedYear,principal,annualInterestRate,tenureMonths,months)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })

        btnGraph.setOnClickListener {
            val i= Intent(this, EmiPaymentAnalysis::class.java)
            i.putExtra("Principal",principal)
            i.putExtra("Interest",annualInterestRate)
            i.putExtra("Duration",tenureMonths)
            startActivity(i)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun addHeaderRow() {
        val headerRow = TableRow(this)
        val headers = arrayOf("Year", "Principal", "Interest", "Total", "Balance", "Loan\nPaid %")
        headers.forEach { title ->
            var tv = TextView(this)
            tv.text = title
            tv.setTypeface(null, Typeface.BOLD)
            tv.gravity = Gravity.CENTER
            tv.setPadding(8, 8, 8, 8)
            tv.setTextColor(android.graphics.Color.WHITE)
            headerRow.addView(tv)
        }
        headerRow.setBackgroundColor(android.graphics.Color.rgb(63,81,181))
        emiTable.addView(headerRow)
    }

    private fun populateYearRows() {
        yearData.forEach { yearSummary ->
            addYearRow(yearSummary)
        }
    }

    private fun addYearRow(yearSummary: EMIYearSummary) {
        val row = TableRow(this)

        val yearText = makeCell(yearSummary.year.toString(), true)
        val principalText = makeCell(yearSummary.totalPrincipal,true)
        val interestText = makeCell(yearSummary.totalInterest,true)
        val totalText = makeCell(yearSummary.totalPayment,true)
        val balanceText = makeCell(yearSummary.balance,true)
        val paidText = makeCell(yearSummary.paidPercent,true)

        row.addView(yearText)
        row.addView(principalText)
        row.addView(interestText)
        row.addView(totalText)
        row.addView(balanceText)
        row.addView(paidText)
        //row.setBackgroundColor(android.graphics.Color.BLUE)

        // Click to expand/collapse months
        row.setOnClickListener {
            if (yearSummary.isExpanded) {
                removeMonthRows(yearSummary.year)
                yearSummary.isExpanded = false
            } else {
                val index = emiTable.indexOfChild(row)
                addMonthRows(yearSummary, index + 1)
                yearSummary.isExpanded = true
            }
        }

        emiTable.addView(row)


    }

    private fun addMonthRows(yearSummary: EMIYearSummary, startIndex: Int) {
        yearSummary.months.forEachIndexed { i, month ->
            val monthRow = TableRow(this)
            monthRow.tag = "month_${yearSummary.year}"

            monthRow.addView(makeCell("  ${month.monthYear}")) // Indent for month
            monthRow.addView(makeCell(month.principal))
            monthRow.addView(makeCell(month.interest))
            monthRow.addView(makeCell(month.totalPayment))
            monthRow.addView(makeCell(month.balance))
            monthRow.addView(makeCell(month.paidPercent))

            emiTable.addView(monthRow, startIndex + i)
        }
    }

    private fun removeMonthRows(year: Int) {
        val rowsToRemove = mutableListOf<View>()
        for (i in 0 until emiTable.childCount) {
            val child = emiTable.getChildAt(i)
            if (child.tag == "month_$year") {
                rowsToRemove.add(child)
            }
        }
        rowsToRemove.forEach { emiTable.removeView(it) }
    }

    private fun makeCell(text: String, bold: Boolean = false): TextView {
        return TextView(this).apply {
            this.text = text
            setPadding(8, 8, 8, 8)
            gravity = Gravity.CENTER
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    private fun refreshTable(month:Int,year:Int,principal: Double,interestRate:Double,tenureMonths: Int,monthList: List<String>){
        emiTable.removeAllViews()
        yearData=generateRepaymentTable(month,year,principal,interestRate,tenureMonths,monthList)
        addHeaderRow()
        populateYearRows()
    }
    private fun generateRepaymentTable(startMonthIndex:Int,startYear:Int,principal:Double,annualRate:Double,tenureMonths: Int,monthList:List<String>): List<EMIYearSummary> {
        var isLast=false
        var repaymentMonthList=mutableListOf<EMIMonthDetail>()
        var repaymentFullList=mutableListOf<EMIYearSummary>()

        val monthlyRate = annualRate / (12 * 100)
        val emi = (principal * monthlyRate * Math.pow(1 + monthlyRate, tenureMonths.toDouble())) /
                (Math.pow(1 + monthlyRate, tenureMonths.toDouble()) - 1)

        var balance = principal
        var loanPaidToDate = 0.0
        var currentYear = startYear
        var monthCounter = startMonthIndex

        var totalPrincipal: Double=0.0
        var totalInterest:Double=0.0
        var totalPayment=0.0

        for (monthIndex in 1..tenureMonths) {
            val interestForMonth = balance * monthlyRate
            val principalForMonth = emi - interestForMonth
            val totalPaymentForMonth=principalForMonth+interestForMonth
            loanPaidToDate += principalForMonth
            balance -= principalForMonth
            totalPrincipal+=principalForMonth
            totalInterest+=interestForMonth
            totalPayment+=emi

            repaymentMonthList.add(EMIMonthDetail(monthList.get(monthCounter),indianCurrencyFormat.format(principalForMonth),indianCurrencyFormat.format(interestForMonth),indianCurrencyFormat.format(totalPaymentForMonth),indianCurrencyFormat.format(balance),String.format("%.2f%%", (loanPaidToDate / principal) * 100.0)))

            monthCounter++
            if (monthCounter>=12) {
                repaymentFullList.add (
                    EMIYearSummary(currentYear,indianCurrencyFormat.format(totalPrincipal),indianCurrencyFormat.format(totalInterest),indianCurrencyFormat.format(totalPayment),indianCurrencyFormat.format(balance),String.format("%.2f%%",(loanPaidToDate/principal)*100.0),repaymentMonthList)
                )
                if(monthIndex==tenureMonths)
                    isLast=true
                repaymentMonthList=mutableListOf<EMIMonthDetail>()
                monthCounter = 0
                currentYear++
                totalPrincipal=0.0
                totalInterest=0.0
                totalPayment=0.0
            }
            if(monthIndex==tenureMonths && !isLast){
                repaymentFullList.add (
                    EMIYearSummary(currentYear,indianCurrencyFormat.format(totalPrincipal),indianCurrencyFormat.format(totalInterest),indianCurrencyFormat.format(totalPayment),indianCurrencyFormat.format(balance),String.format("%.2f%%",(loanPaidToDate/principal)*100.0),repaymentMonthList)
                )
                break
            }
        }
        return repaymentFullList

    }

    private val indianCurrencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
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
    fun receiveTenure():String{
        val bundle:Bundle?=intent.extras
        val tenure=bundle?.getString("Tenure").toString()
        return tenure
    }
}