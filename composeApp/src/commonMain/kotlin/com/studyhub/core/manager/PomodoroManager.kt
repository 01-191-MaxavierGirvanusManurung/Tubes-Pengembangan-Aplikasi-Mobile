package com.studyhub.core.manager

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class PomodoroMode { WORK, BREAK }

data class PomodoroState(
    val secondsRemaining: Int = 25 * 60,
    val isRunning: Boolean = false,
    val mode: PomodoroMode = PomodoroMode.WORK,
    val sessionsCompleted: Int = 0,
    val workDurationMins: Int = 25,
    val breakDurationMins: Int = 5
)

class PomodoroManager {
    private val _state = MutableStateFlow(PomodoroState())
    val state: StateFlow<PomodoroState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun startTimer() {
        if (_state.value.isRunning) return
        _state.update { it.copy(isRunning = true) }
        
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _state.value.isRunning && _state.value.secondsRemaining > 0) {
                delay(1000)
                _state.update { it.copy(secondsRemaining = it.secondsRemaining - 1) }
            }
            if (_state.value.secondsRemaining == 0) {
                handleTimerFinished()
            }
        }
    }

    fun pauseTimer() {
        _state.update { it.copy(isRunning = false) }
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _state.update { 
            val duration = if (it.mode == PomodoroMode.WORK) it.workDurationMins else it.breakDurationMins
            it.copy(secondsRemaining = duration * 60) 
        }
    }

    fun skipMode() {
        handleTimerFinished()
    }

    fun setWorkDuration(minutes: Int) {
        _state.update { 
            val newState = it.copy(workDurationMins = minutes)
            if (it.mode == PomodoroMode.WORK && !it.isRunning) {
                newState.copy(secondsRemaining = minutes * 60)
            } else newState
        }
    }

    fun setBreakDuration(minutes: Int) {
        _state.update { 
            val newState = it.copy(breakDurationMins = minutes)
            if (it.mode == PomodoroMode.BREAK && !it.isRunning) {
                newState.copy(secondsRemaining = minutes * 60)
            } else newState
        }
    }

    fun setMode(mode: PomodoroMode) {
        pauseTimer()
        _state.update { 
            val duration = if (mode == PomodoroMode.WORK) it.workDurationMins else it.breakDurationMins
            it.copy(mode = mode, secondsRemaining = duration * 60) 
        }
    }

    private fun handleTimerFinished() {
        _state.update { 
            if (it.mode == PomodoroMode.WORK) {
                it.copy(
                    mode = PomodoroMode.BREAK,
                    secondsRemaining = it.breakDurationMins * 60,
                    isRunning = false,
                    sessionsCompleted = it.sessionsCompleted + 1
                )
            } else {
                it.copy(
                    mode = PomodoroMode.WORK,
                    secondsRemaining = it.workDurationMins * 60,
                    isRunning = false
                )
            }
        }
        timerJob?.cancel()
    }
}
