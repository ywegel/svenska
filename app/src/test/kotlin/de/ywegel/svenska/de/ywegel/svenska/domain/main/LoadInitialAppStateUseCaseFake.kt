package de.ywegel.svenska.de.ywegel.svenska.domain.main

import de.ywegel.svenska.domain.main.InitialAppState
import de.ywegel.svenska.domain.main.LoadInitialAppStateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class LoadInitialAppStateUseCaseFake(initialAppState: InitialAppState) : LoadInitialAppStateUseCase {
    private val flow = MutableStateFlow(initialAppState)

    override fun invoke(): Flow<InitialAppState> = flow

    fun emitNewState(state: InitialAppState) {
        flow.value = state
    }
}
