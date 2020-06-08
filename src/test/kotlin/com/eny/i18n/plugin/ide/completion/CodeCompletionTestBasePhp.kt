package com.eny.i18n.plugin.ide.completion

import com.eny.i18n.plugin.utils.generator.code.CodeGenerator
import com.eny.i18n.plugin.utils.generator.code.PhpCodeGenerator
import com.eny.i18n.plugin.utils.generator.translation.JsonTranslationGenerator
import com.eny.i18n.plugin.utils.generator.translation.TranslationGenerator
import com.eny.i18n.plugin.utils.generator.translation.YamlTranslationGenerator
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

internal abstract class CodeCompletionTestBasePhp(
    private val codeGenerator: CodeGenerator,
    private val translationGenerator: TranslationGenerator,
    private val keyGenerator: KeyGenerator,
    checkerProducer: (fixture: CodeInsightTestFixture) -> Checker = ::NsChecker) :
        CodeCompletionTestBase(codeGenerator, translationGenerator, keyGenerator, checkerProducer) {

    fun testDQuote() = checker.doCheck(
        "dQuote.${codeGenerator.ext()}",
        codeGenerator.generate(keyGenerator.generate("test", "tst1.base.<caret>", "\"")),
        codeGenerator.generate(keyGenerator.generate("test","tst1.base.single", "\"")),
        translationGenerator.ext(),
        translationGenerator.generateContent("tst1", "base", "single", "only one value")
    )
}

internal class CodeCompletionPhpJsonTest: CodeCompletionTestBasePhp(PhpCodeGenerator(), JsonTranslationGenerator(), NsKeyGenerator())
internal class CodeCompletionPhpYamlDefNsTest: CodeCompletionTestBasePhp(PhpCodeGenerator(), YamlTranslationGenerator(), DefaultNsKeyGenerator(), ::DefaultNsChecker)
