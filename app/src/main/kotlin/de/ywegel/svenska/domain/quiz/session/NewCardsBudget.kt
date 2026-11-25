package de.ywegel.svenska.domain.quiz.session

fun newWordsAllowedToday(introducedToday: Int, dueCount: Int, newPerDay: Int, backlogLimit: Int): Int =
    if (dueCount > backlogLimit) 0 else (newPerDay - introducedToday).coerceAtLeast(0)
