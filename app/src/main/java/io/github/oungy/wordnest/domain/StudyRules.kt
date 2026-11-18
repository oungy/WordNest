package io.github.oungy.wordnest.domain

internal enum class Rating { Forgot, Unsure, Remembered }

internal const val ReviewRuleVersion = 1

internal fun reviewInterval(rating: Rating, previousDays: Int?): Int = when (rating) {
    Rating.Forgot -> 1
    Rating.Unsure -> 2
    Rating.Remembered -> ((previousDays ?: 0) * 2).coerceIn(3, 30)
}

internal fun selectNewWords(books: List<List<String>>, limit: Int): List<String> {
    val queues = books.map { it.iterator() }
    val selected = linkedSetOf<String>()
    while (selected.size < limit) {
        val beforeRound = selected.size
        for (queue in queues) {
            while (queue.hasNext()) {
                if (selected.add(queue.next())) break
            }
            if (selected.size == limit) break
        }
        if (selected.size == beforeRound) break
    }
    return selected.toList()
}
