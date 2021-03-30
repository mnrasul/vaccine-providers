package com.example.vaccine.aws

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awscdk.core.App
import software.amazon.awscdk.core.CfnOutput
import software.amazon.awscdk.core.Construct
import software.amazon.awscdk.core.Duration
import software.amazon.awscdk.core.RemovalPolicy
import software.amazon.awscdk.core.Tags
import software.amazon.awscdk.services.apigateway.IntegrationResponse
import software.amazon.awscdk.services.apigateway.LambdaIntegration
import software.amazon.awscdk.services.apigateway.LambdaIntegrationOptions
import software.amazon.awscdk.services.apigateway.MethodOptions
import software.amazon.awscdk.services.apigateway.MethodResponse
import software.amazon.awscdk.services.apigateway.RestApi
import software.amazon.awscdk.services.apigateway.UsagePlanPerApiStage
import software.amazon.awscdk.services.apigateway.UsagePlanProps
import software.amazon.awscdk.services.iam.IRole
import software.amazon.awscdk.services.iam.Role
import software.amazon.awscdk.services.lambda.*
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.ssm.StringParameter
import software.amazon.awscdk.services.ssm.StringParameterProps
import java.util.*

/**
 * Largely here for interop with maven exec plugin. How do we want this to run long term?
 * TODO: .env generation
 */
class LoanProvidersApp {
    companion object {

        //128 char limit: https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cfn-using-console-create-stack-parameters.html
        const val applicationName = "loan-providers"

        @JvmStatic
        fun main(args: Array<String>) {
            val app = App()
            LoanProvidersCDK(app, StackConfiguration.fromCliArgs(args))

            //looks like it requires a commit/tag to exist first.
//            Tags.of(app).add("branchName", OS.branch)
            app.synth()
        }

        private fun determineEnvName(args: Array<String>): String {
            return if (args.isNotEmpty()) {
                args[0]
            } else {
                OS.whoAmI
            }
        }
    }
}

object OS {
    // Cloud formations does not support . characters
    val whoAmI: String
        get() = String(java.lang.Runtime.getRuntime().exec("whoami").inputStream.readBytes()).replace(".", "-").trim()
    val branch: String
            by lazy {
                System.getenv("GITHUB_HEAD_REF")
            }
}

/**
 * In principal, we could have a different version for each alias, but that would complicate things alot.
 * We'll stick to same version for all functions in a release. Over time it could result in lots of versions, but
 * in practice, we only pay for actual invocations.
 *
 */
data class FunctionDefinition(
    val name: String,
    val handler: String
)

class LoanProvidersCDK(
    scope: Construct,
    private val stackConfiguration: StackConfiguration
) : software.amazon.awscdk.core.Stack(scope, stackConfiguration.stackName, null) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val functions = listOf(
        FunctionDefinition("LoanProviderFn", "LoanProviderFnHandler")
    )

    private val envName: String
        get() = this.stackName.split("-").first()

    init {
        functions.forEach {
            buildFunction(it)
        }
    }

    private fun buildFunction(definition: FunctionDefinition): Function {
        val function = Function.Builder.create(this, definition.name)
            .runtime(Runtime.PROVIDED)
            .handler("not.used.by.quarkus.in.native.mode")
            .code(Code.fromAsset("target/function.zip"))
            .environment(
                mapOf(
                    "DISABLE_SIGNAL_HANDLERS" to "true",            // required by graal native
                    "QUARKUS_LAMBDA_HANDLER" to definition.handler  // https://quarkus.io/guides/amazon-lambda#choose
                )
            )
//                stack is deleted when the name changes
            .functionName("$stackName-${definition.name}")
            .tracing(Tracing.ACTIVE)
            .currentVersionOptions(versionOptions())
            .timeout(Duration.seconds(100))  // What's a good timeout. No API gateway restriction...
            .memorySize(1024)
            .build()
        val version = function.currentVersion
        val aliasName = "Alias-${definition.name}"
        logger.info("Current Version is: ${version.latestVersion.version}")
        logger.info("AliasName to set: $aliasName")
        val alias = Alias.Builder.create(this, aliasName)
            .aliasName(aliasName)
            .version(version)
        grantInvokeFunctionPermissionsToBlackbirdAndLightmile(function)
        buildStackOutputs(function, definition)


        val versionParameterProps = StringParameterProps.builder()
            .parameterName("/$stackName-${definition.name}/LATEST_VERSION")
            .stringValue(version.version)
            .build()

        val versionParameter = StringParameter(this, "${definition.name}LatestVersion", versionParameterProps)

        versionParameter.grantRead(Role.fromRoleArn(this, "VersionReadableArn", function.role!!.roleArn))
//        createRestApi(definition, function)
        return function
    }

    private fun versionOptions(): VersionOptions {
        return VersionOptions.builder()
            .description("${UUID.randomUUID()}")
            .removalPolicy(RemovalPolicy.RETAIN)
            .build()
    }

    private fun buildStackOutputs(function: Function, definition: FunctionDefinition) {
        CfnOutput.Builder.create(this, "${definition.name}Output")
            .exportName("${stackConfiguration.envName}-${definition.name}")
            .value(function.functionArn)
            .build()
    }

    private fun grantInvokeFunctionPermissionsToBlackbirdAndLightmile(function: Function) {
        if (stackConfiguration.sunrunApplicationEnvironment.isEphemeral) {
            logger.info("Skipping IAM policies because this is an ephemeral env.")
            return
        }

        function.grantInvoke(ecsTaskRole("${stackConfiguration.envName}-ecs-task-blackbird"))
        function.grantInvoke(ecsTaskRole("${stackConfiguration.envName}-ecs-task-pricing-blackbird"))
        function.grantInvoke(ecsTaskRole("${stackConfiguration.envName}-ecs-task-lightmile"))
        function.grantInvoke(ecsTaskRole("${stackConfiguration.envName}-ecs-task-pricing-lightmile"))
    }

    private fun ecsTaskRole(id: String): IRole {
        val awsAccountNumber = stackConfiguration.sunrunApplicationEnvironment.sunrunAwsAccount.accountNumber
        // lifted from https://github.com/SunRun/zopio/blob/master/CF/Zopio_apppolicy.yaml
        return Role.fromRoleArn(this, id, "arn:aws:iam::${awsAccountNumber}:role/${id}")
    }

    private fun createRestApi(
        definition: FunctionDefinition,
        function: Function
    ) {
        val api = RestApi.Builder.create(this, "$stackName-${definition.name}-api").build()

        val integration = LambdaIntegration(
            function, LambdaIntegrationOptions.builder()
                .proxy(false)
                .integrationResponses(
                    listOf(
                        IntegrationResponse.builder()
                            .statusCode("200").build()
                    )
                )
                .build()
        )
        val options = MethodOptions.builder()
            .apiKeyRequired(true)
            .methodResponses(listOf(MethodResponse.builder().statusCode("200").build()))
            .build()
        api.root.addMethod("ANY", integration, options)

        val devApiKey = api.addApiKey("$stackName-Developers")
        api.addUsagePlan(
            "$stackName-Developers-Usage",
            UsagePlanProps.builder()
                .apiKey(devApiKey)
                .apiStages(listOf(UsagePlanPerApiStage.builder().api(api).stage(api.deploymentStage).build()))
                .build()
        )

        val gsheetApiKey = api.addApiKey("$stackName-GSheet")
        api.addUsagePlan(
            "$stackName-GSheet-Usage",
            UsagePlanProps.builder()
                .apiKey(gsheetApiKey)
                .apiStages(listOf(UsagePlanPerApiStage.builder().api(api).stage(api.deploymentStage).build()))
                .build()
        )
    }
    private fun createApi() {
        RestApi.Builder.create(this, "${stackConfiguration.envName}-")
    }
}

data class StackConfiguration(
    val envName: String,
    val stackName: String,
    val sunrunApplicationEnvironment: SunrunApplicationEnvironment
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    init {
        logger.info("Synthesizing $this")
    }

    companion object {
        fun fromCliArgs(args: Array<String>): StackConfiguration {
            val envName = if (args.isNotEmpty()) {
                args[0]
            } else {
                OS.whoAmI
            }

            val stackName = "${envName}-${LoanProvidersApp.applicationName}"
            val applicationEnvironment = SunrunApplicationEnvironment.valueOfCaseInsensitive(envName)

            return StackConfiguration(envName, stackName, applicationEnvironment)
        }
    }
}



