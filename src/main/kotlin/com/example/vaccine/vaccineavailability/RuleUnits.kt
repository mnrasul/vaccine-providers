package com.example.vaccine.vaccineavailability

import com.example.vaccine.VaccineOption
import com.example.vaccine.VaccineProviderInfoRequest
import org.kie.kogito.rules.RuleUnitData
import org.kie.kogito.rules.SingletonStore
import org.kie.kogito.rules.units.ListDataStream

class VaccineAvailabilityService(
  val inputObject: SingletonStore<VaccineProviderInfoRequest>,
  val outputObject: ListDataStream<VaccineOption>
) : RuleUnitData
