package com.example.emicalculator

data class EMIYearSummary(
    val year: Int,
    val totalPrincipal: String,
    val totalInterest: String,
    val totalPayment: String,
    val balance: String,
    val paidPercent: String,
    val months: List<EMIMonthDetail>,
    var isExpanded: Boolean = false
)

