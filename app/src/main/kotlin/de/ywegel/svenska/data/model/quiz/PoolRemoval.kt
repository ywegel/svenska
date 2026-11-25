package de.ywegel.svenska.data.model.quiz

/**
 * What removing a word from the quiz pool did, decided on the stored state at the time of the removal.
 *
 * - [DELETED]: the word was queued and had no progress, so it is no longer in the pool.
 * - [PAUSED]: the word was active. It is paused and keeps its progress.
 * - [UNCHANGED]: the word was already paused or not in the pool.
 */
enum class PoolRemoval { DELETED, PAUSED, UNCHANGED }
