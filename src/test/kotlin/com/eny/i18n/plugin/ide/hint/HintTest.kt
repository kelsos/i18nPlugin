package com.eny.i18n.plugin.ide.hint

import com.eny.i18n.plugin.PlatformBaseTest
import com.eny.i18n.plugin.ide.CodeTranslationGenerators
import com.eny.i18n.plugin.ide.runVue
import com.eny.i18n.plugin.utils.generator.code.*
import com.eny.i18n.plugin.utils.generator.translation.JsonTranslationGenerator
import com.eny.i18n.plugin.utils.generator.translation.TranslationGenerator
import com.intellij.codeInsight.documentation.DocumentationManager
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class HintTest: PlatformBaseTest() {

    @Test
    fun testSingleHint() {
        val cgs = listOf(JsCodeGenerator(), TsCodeGenerator(), JsxCodeGenerator(), TsxCodeGenerator(), PhpSingleQuoteCodeGenerator(), PhpDoubleQuoteCodeGenerator())
        val translation = "translation here"
        val tg = JsonTranslationGenerator()
        cgs.forEachIndexed {
            index, cg ->
                myFixture.addFileToProject("test${index}.${tg.ext()}", tg.generateContent("root", "first", "second", translation))
                myFixture.configureByText("content.${cg.ext()}", cg.generate("\"test${index}:root.first.<caret>second\""))
                read {
                    val codeElement = myFixture.file.findElementAt(myFixture.caretOffset)
                    assertEquals(
                        translation,
                        DocumentationManager.getProviderFromElement(codeElement)
                            .getQuickNavigateInfo(myFixture.elementAtCaret, codeElement)
                    )
                }
        }
    }

    @Test
    fun vueSfcHint() = myFixture.runVue {
        val cg = VueCodeGenerator()
        val tg = JsonTranslationGenerator()
        myFixture.configureByText(
            "App.vue",
            cg.generateSfcBlock(
                "<p>{{ \$t('root.first.<caret>second')}}</p>",
                tg.generateNamedBlock("en", tg.generateContent("root", "first", "second", "translation here"))
            )
        )
        read {
            val codeElement = myFixture.file.findElementAt(myFixture.caretOffset)
            //Not supported for Vue SFC:
            assertNull(
                DocumentationManager.getProviderFromElement(codeElement).getQuickNavigateInfo(myFixture.elementAtCaret, codeElement)
            )
        }
    }
}
