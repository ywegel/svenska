package de.ywegel.svenska.data.model.quiz

/**
 * The state of a word in the quiz pool. A word that is not in the pool has no pool entry and no status.
 *
 * - [QUEUED]: waiting to be introduced, at most the daily limit of new cards per day and none on a day with a backlog.
 * - [ACTIVE]: introduced, its unlocked facets are quizzed when due.
 * - [PAUSED]: removed after its introduction, not quizzed, progress kept.
 */
enum class QuizPoolStatus { QUEUED, ACTIVE, PAUSED }
