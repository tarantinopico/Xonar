package com.tarantino.xonarx.domain.usecase

class SpeedEtaCalculator {
    fun calculateSpeed(bytesSinceLastUpdate: Long, timeDeltaMs: Long): Long {
        if (timeDeltaMs <= 0) return 0L
        return bytesSinceLastUpdate * 1000 / timeDeltaMs
    }

    fun calculateEta(remainingBytes: Long, speedBytesPerSecond: Long): Long {
        if (speedBytesPerSecond <= 0) return -1L
        if (remainingBytes <= 0) return 0L
        return remainingBytes / speedBytesPerSecond
    }
}
