package com.example.vaccine.aws

import com.example.vaccine.aws.AwsServiceClientProducer
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest

// can add a SecretsProvider instead if it's a secrets manager
val ssmProvider: SsmClient by lazy {
    SsmClient.builder().httpClient(AwsServiceClientProducer().apacheClient()).build()
}

val currentRunningLambdaVersion: String by lazy {
    val funcName = System.getenv("AWS_LAMBDA_FUNCTION_NAME")
    val envVersion = System.getenv("AWS_LAMBDA_FUNCTION_VERSION")
    """${funcName}:${
        when {
            envVersion != "\$LATEST" -> envVersion
            else -> {
                val response = ssmProvider.getParameter(
                    GetParameterRequest.builder().name("""/${funcName}/LATEST_VERSION""").build()
                )
                response.parameter().value()
            }
        }
    }"""
}

fun getLambdaVersion(): String {
    return currentRunningLambdaVersion
}
