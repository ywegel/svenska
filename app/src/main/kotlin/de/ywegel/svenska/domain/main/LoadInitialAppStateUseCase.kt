package de.ywegel.svenska.domain.main

import kotlinx.coroutines.flow.Flow

interface LoadInitialAppStateUseCase {
    operator fun invoke(): Flow<InitialAppState>
}
