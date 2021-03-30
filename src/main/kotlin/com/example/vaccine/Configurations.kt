package com.example.vaccine

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.quarkus.jackson.ObjectMapperCustomizer
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Singleton

/**
 * https://quarkus.io/guides/rest-json#jackson
 */
@Singleton
class JacksonConfig : ObjectMapperCustomizer {
  override fun customize(mapper: ObjectMapper) {
    mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.registerModule(KotlinModule.Builder().nullIsSameAsDefault(true).build())
  }

  fun objectMapper(): ObjectMapper {
    val mapper = jacksonObjectMapper()
    customize(mapper)
    return mapper
  }
}


class BigDecimalSerialization : JsonSerializer<BigDecimal?>() {
  @Throws(IOException::class)
  override fun serialize(value: BigDecimal?, jgen: JsonGenerator, provider: SerializerProvider?) {
    // Using writeNumber and removing toString make sure the output is number but not String.
    jgen.writeNumber(value?.setScale(DEFAULT_SCALE_FOR_CURRENCY, RoundingMode.HALF_UP))
  }

}
