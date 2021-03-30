package com.example.vaccine

import com.example.vaccine.Apr
import com.example.vaccine.DealerFee
import com.example.vaccine.VaccineOption
import com.example.vaccine.VaccineProviderInfoRequest
import com.example.vaccine.Rule
import com.example.vaccine.Term
import com.example.vaccine.Vendor
import java.math.BigDecimal

const val DEFAULT_STATE_CODE = "CA"

fun vaccineOption(
  vendor: Vendor = "",
  count: Int = 0,
  rule: Rule = "",
) = VaccineOption(vendor, count, rule)



fun vaccineProviderInfoRequest(state: String = DEFAULT_STATE_CODE, fruit: String = "")
= VaccineProviderInfoRequest(state, fruit)
