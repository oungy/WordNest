package io.github.oungy.wordnest.domain.library

import java.text.Normalizer
import java.util.Locale

internal object NotebookRules {
    fun normalizeName(name: String): String = Normalizer.normalize(name, Normalizer.Form.NFKC).trim()

    fun nameKey(name: String): String = normalizeName(name).lowercase(Locale.ROOT)

    fun isValidName(name: String): Boolean {
        val normalized = normalizeName(name)
        return normalized.codePointCount(0, normalized.length) in 1..32
    }
}
