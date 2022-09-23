package com.github.youopensource.youjetbrainsearch.services

import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder

object TelemetryService {

    init {
    }

    val instance: TelemetryMessageBuilder by lazy {
        TelemetryMessageBuilder(TelemetryService::class.java.classLoader)
    }

}
