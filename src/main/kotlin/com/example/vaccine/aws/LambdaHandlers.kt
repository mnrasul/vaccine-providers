package com.example.vaccine.aws

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.example.vaccine.JacksonConfig
import com.example.vaccine.VaccineOption
import com.example.vaccine.VaccineOptions
import com.example.vaccine.VaccineProviderInfoRequest
import com.example.vaccine.Rule
import com.example.vaccine.vaccineavailability.VaccineAvailabilityService
import com.example.vaccine.vaccineavailability.VaccineAvailabilityServiceQueryVaccineAvailabilityQueryEndpoint
import org.kie.kogito.rules.units.FieldDataStore
import org.kie.kogito.rules.units.ListDataStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Named

@Named("VaccineProviderFnHandler")
class VaccineProviderFnHandler : RequestHandler<VaccineProviderInfoRequest, VaccineOptions> {
  private val logger: Logger = LoggerFactory.getLogger(this::class.java)

  @Inject
  //quarkus wants @inject items to be package private
  lateinit var objectMapper: JacksonConfig

  @Inject
  lateinit var resource: VaccineAvailabilityServiceQueryVaccineAvailabilityQueryEndpoint


  override fun handleRequest(request: VaccineProviderInfoRequest, context: Context?): VaccineOptions {
    logger.info("Input Request : ${objectMapper.objectMapper().writeValueAsString(request)}")
    val inputObject = FieldDataStore<VaccineProviderInfoRequest>().apply {
      set(request)
    }
    val outputObject = ListDataStream<VaccineOption>()
    val ruleUnitObject = VaccineAvailabilityService(inputObject, outputObject)
    val matches = resource.executeQuery(ruleUnitObject)

    return VaccineOptions(getLambdaVersion(), matches)
  }
}
