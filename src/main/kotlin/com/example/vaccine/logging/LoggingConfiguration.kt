package com.example.vaccine.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

//import kotlin.reflect.full.companionObject

fun getLogger(forClass: Class<*>): Logger = LoggerFactory.getLogger(forClass)

fun <T : Any> getClassForLogging(javaClass: Class<T>): Class<*> {
    return javaClass.enclosingClass?.takeIf {
        it.kotlin.java == javaClass
    } ?: javaClass
}

fun <T : Any> T.getLogger(): Logger = getLogger(getClassForLogging(javaClass))
