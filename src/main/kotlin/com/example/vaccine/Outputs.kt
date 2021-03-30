package com.example.vaccine

import java.math.BigDecimal
import java.math.BigDecimal.ZERO

typealias DealerFee = BigDecimal
typealias Apr = BigDecimal
typealias Vendor = String
typealias Rule = String
typealias Term = Int

/**
 * Response structure when there's a bad request
 */
data class ErrorResponse(
  val errorType: String = "",
  val errorMessage: String = ""
)


data class VaccineOptions(
  val version: String = "",
  val vaccineOptions: List<VaccineOption> = listOf()
)

data class VaccineOption(
  var vendor: Vendor = "",
  var count: Term = 0,
  var rule: String = "",
) {
  /**
   * Overrides to use String to avoid putting BigDecimal and Integer parsing in XLSX
   */
  fun setTerm(term: String) {
    this.count = Integer.parseInt(term).toInt()
  }
}
