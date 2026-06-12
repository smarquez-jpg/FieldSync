package com.fieldsync

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FieldSyncApplication

fun main(args: Array<String>) {
    runApplication<FieldSyncApplication>(*args)
}
