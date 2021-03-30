package com.example.vaccine.aws

import software.amazon.awscdk.core.RemovalPolicy

enum class SunrunAwsAccount(val accountNumber: Long) {
    Dev(563116987804), Stage(339585210943), Prd(578915239930)
}

enum class SunrunApplicationEnvironment(val sunrunAwsAccount: SunrunAwsAccount, val isEphemeral: Boolean) {
    Sandbox(SunrunAwsAccount.Dev, true),
    Devmaj(SunrunAwsAccount.Dev, false),
    Relcert(SunrunAwsAccount.Stage, false),
    Majstg(SunrunAwsAccount.Prd, false),
    Prd(SunrunAwsAccount.Prd, false);

    val removalPolicy: RemovalPolicy
        get() = if (isEphemeral) RemovalPolicy.DESTROY else RemovalPolicy.RETAIN

    companion object {
        private val valuesByNameCaseInsensitive = values().associateBy { it.name.toLowerCase().trim() }

        fun valueOfCaseInsensitive(value: String): SunrunApplicationEnvironment {
            return valuesByNameCaseInsensitive.getOrDefault(value.toLowerCase().trim(), Sandbox)
        }
    }
}
