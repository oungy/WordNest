package io.github.oungy.wordnest.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StudyRulesTest {
    @Test
    fun roundRobinSkipsSharedWordsAndUsesOtherBooksWhenOneRunsOut() {
        val books = listOf(listOf("shared", "first"), listOf("shared", "second", "third", "fourth"))
        assertEquals(listOf("shared", "second", "first", "third"), selectNewWords(books, 4))
        assertEquals(listOf("shared", "second", "first", "third", "fourth"), selectNewWords(books, 20))
    }

    @Test
    fun singleBookKeepsContentOrderAndEmptySelectionProducesNoWords() {
        assertEquals(listOf("book-noun", "book-verb"), selectNewWords(listOf(listOf("book-noun", "book-verb")), 2))
        assertEquals(emptyList<String>(), selectNewWords(emptyList(), 5))
    }

    @Test
    fun reviewIntervalsReactToRecallAndStayBounded() {
        assertEquals(1, reviewInterval(Rating.Forgot, 14))
        assertEquals(2, reviewInterval(Rating.Unsure, 14))
        assertEquals(3, reviewInterval(Rating.Remembered, null))
        assertEquals(6, reviewInterval(Rating.Remembered, 3))
        assertEquals(30, reviewInterval(Rating.Remembered, 24))
    }
}
