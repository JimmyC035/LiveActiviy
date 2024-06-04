package com.example.liveactivty

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProcessCounter {

    private var processValue: Int = 0
    private var isRunning: Boolean = true

    fun start(): Flow<Int> = flow {
        while (isRunning){
            emit(processValue)
            delay(1000L)
            processValue += 1
        }
    }

    fun stop(){
        isRunning = false
        processValue = 0
    }



}