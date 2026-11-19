package io.github.oungy.wordnest.domain.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotebookRulesTest {
    @Test
    fun normalizesBeforeCheckingIdentityAndLength() {
        assertEquals("Café", NotebookRules.normalizeName("  Cafe\u0301　"))
        assertEquals(NotebookRules.nameKey("旅行 abc"), NotebookRules.nameKey("　旅行 ＡＢＣ "))
        assertFalse(NotebookRules.isValidName("　 "))
        assertTrue(NotebookRules.isValidName("🌱".repeat(32)))
        assertFalse(NotebookRules.isValidName("🌱".repeat(33)))
    }
}
