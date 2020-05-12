package com.eny.i18n.plugin.ide.actions

import com.eny.i18n.plugin.ide.settings.Settings

class ExtractionCancellation: ExtractionTestBase() {

    fun testTsCancel() {
        val settings = Settings.getInstance(myFixture.project)
        settings.vue = false
        doCancel("js/simple.js", "assets/test.json")
    }

    fun testTsCancelInvalid() {
        val settings = Settings.getInstance(myFixture.project)
        settings.vue = false
        doCancelInvalid("ts/simple.ts", "assets/test.json")
    }

    fun testExtractionUnavailable() {
        val settings = Settings.getInstance(myFixture.project)
        settings.vue = false
        doUnavailable("tsx/unavailable.tsx")
    }

    fun testInvalidSource() {
        val settings = Settings.getInstance(myFixture.project)
        settings.vue = false
        doRun("jsx/strange.jsx",
            "jsx/strangeKeyExtracted.jsx",
            "assets/test.json",
            "assets/testKeyExtracted.json",
            "test:ref.value3"
        )
    }
}

abstract class ExtractI18nIntentionActionBase(private val language: String, private val translationFormat: String): ExtractionTestBase() {

    fun testKeyExtraction() {
        doRun(
            "$language/simple.$language",
            "$language/simpleKeyExtracted.$language",
            "assets/test.$translationFormat",
            "assets/testKeyExtracted.$translationFormat",
            "test:ref.value3")
    }

    fun testDefNsKeyExtraction() {
        doRun(
            "$language/simple.$language",
            "$language/simpleDefNsKeyExtracted.$language",
            "assets/translation.$translationFormat",
            "assets/translationKeyExtracted.$translationFormat",
            "ref.value.sub1")
    }

    fun testKeyContext() {
        doUnavailable("$language/keyContext.$language")
    }
}

class ExtractI18nIntentionActionJsJsonTest: ExtractI18nIntentionActionBase("js","json")
class ExtractI18nIntentionActionTsJsonTest: ExtractI18nIntentionActionBase("ts", "json")
class ExtractI18nIntentionActionTsxJsonTest: ExtractI18nIntentionActionBase("tsx", "json")
class ExtractI18nIntentionActionJsxJsonTest: ExtractI18nIntentionActionBase("jsx", "json")
class ExtractI18nIntentionActionJsYamlTest: ExtractI18nIntentionActionBase("js","yml")
class ExtractI18nIntentionActionTsYamlTest: ExtractI18nIntentionActionBase("ts", "yml")
class ExtractI18nIntentionActionTsxYamlTest: ExtractI18nIntentionActionBase("tsx", "yml")
class ExtractI18nIntentionActionJsxYamlTest: ExtractI18nIntentionActionBase("jsx", "yml")

abstract class ExtractI18nIntentionActionPhpBase(private val translationFormat: String): ExtractI18nIntentionActionBase("php", translationFormat) {

    fun testKeyExtractionSingleQuoted() {
        doRun(
            "php/simpleSingleQuoted.php",
            "php/simpleKeyExtracted.php",
            "assets/test.$translationFormat",
            "assets/testKeyExtracted.$translationFormat",
            "test:ref.value3")
    }

    fun testDefNsKeyExtractionSingleQuoted() {
        doRun(
            "php/simpleSingleQuoted.php",
            "php/simpleDefNsKeyExtracted.php",
            "assets/translation.$translationFormat",
            "assets/translationKeyExtracted.$translationFormat",
            "ref.value.sub1")
    }
}
class ExtractI18nIntentionActionPhpJsonTest: ExtractI18nIntentionActionPhpBase("json")
class ExtractI18nIntentionActionPhpYamlTest: ExtractI18nIntentionActionPhpBase("yml")

abstract class ExtractI18nIntentionActionVueI18nBase(private val translationFormat: String): ExtractionTestBase() {
//
//    override fun setUp() {
//        super.setUp()
//        val settings = Settings.getInstance(myFixture.project)
//        settings.vue = true
//    }

    fun testKeyExtraction() {
        val settings = Settings.getInstance(myFixture.project)
        settings.vue = true
        doRun(
            "vue/simpleVue.vue",
            "vue/simpleKeyExtractedVue.vue",
            "locales/en-US.$translationFormat",
            "locales/en-USKeyExtracted.$translationFormat",
            "ref.value3"
        )
        settings.vue = false
    }

    fun testKeyExtraction2() {
        val settings = Settings.getInstance(myFixture.project)
        settings.vue = true
        doRun(
            "vue/App.vue",
            "vue/AppExtracted.vue",
            "locales/en-US.$translationFormat",
            "locales/en-USKeyExtracted.$translationFormat",
            "ref.value3"
        )
        settings.vue = false
    }

    fun testScriptKeyExtraction() {
        val settings = Settings.getInstance(myFixture.project)
        settings.vue = true
        doRun(
            "vue/scriptVue.vue",
            "vue/scriptKeyExtractedVue.vue",
            "locales/en-US.$translationFormat",
            "locales/en-USKeyExtracted.$translationFormat",
            "ref.value3"
        )
        settings.vue = false
    }
}

//class ExtractI18nIntentionActionVueI18nJsonTest: ExtractI18nIntentionActionVueI18nBase("json")
//class ExtractI18nIntentionActionVueI18nYamlTest: ExtractI18nIntentionActionVueI18nBase("yml")