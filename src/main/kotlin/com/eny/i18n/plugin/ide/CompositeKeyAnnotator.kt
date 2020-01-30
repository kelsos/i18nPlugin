package com.eny.i18n.plugin.ide

import com.eny.i18n.plugin.ide.settings.Settings
import com.eny.i18n.plugin.parser.FullKeyExtractor
import com.eny.i18n.plugin.tree.CompositeKeyResolver
import com.eny.i18n.plugin.tree.PsiElementTree
import com.eny.i18n.plugin.utils.AnnotationHelper
import com.eny.i18n.plugin.utils.AnnotationHolderFacade
import com.eny.i18n.plugin.utils.FullKey
import com.eny.i18n.plugin.utils.LocalizationFileSearch
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

/**
 * Annotator for i18n keys
 */
class CompositeKeyAnnotator : Annotator, CompositeKeyResolver<PsiElement> {

    private val keyExtractor = FullKeyExtractor()

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val i18nKeyLiteral = keyExtractor.extractI18nKeyLiteral(element)
        if (i18nKeyLiteral != null) {
            annotateI18nLiteral(i18nKeyLiteral, element, holder)
        }
    }

    private fun annotateI18nLiteral(fullKey: FullKey, element: PsiElement, holder: AnnotationHolder) {
        val fileName = fullKey.ns?.text
        val compositeKey = fullKey.compositeKey
        val annotationHelper = AnnotationHelper(holder, AnnotationHolderFacade(element.textRange))
        val files = LocalizationFileSearch(element.project).findFilesByName(fileName)
        val settings = Settings.getInstance(element.project)
        if (files.isEmpty()) {
            val isVueContext = settings.vue && element.containingFile.name.substringAfter(".").equals("vue", true)
            if (isVueContext) {
                annotationHelper.annotateUnresolvedVueKey(fullKey)
            } else {
                annotationHelper.annotateFileUnresolved(fullKey)
            }
        }
        else {
            val pluralSeparator = settings.pluralSeparator
            val mostResolvedReference = files
                .flatMap { jsonFile ->
                    tryToResolvePlural(
                        resolveCompositeKey(
                            compositeKey,
                            PsiElementTree.create(jsonFile)
                        ),
                        pluralSeparator
                    )
                }
                .maxBy {v -> v.path.size}!!
            when {
                mostResolvedReference.unresolved.isEmpty() && mostResolvedReference.element?.isLeaf() ?: false -> annotationHelper.annotateResolved(fullKey)
                mostResolvedReference.unresolved.isEmpty() && mostResolvedReference.isPlural-> annotationHelper.annotateReferenceToPlural(fullKey)
                mostResolvedReference.unresolved.isEmpty() && fullKey.isTemplate -> annotationHelper.annotatePartiallyResolved(fullKey, mostResolvedReference.path)
                mostResolvedReference.unresolved.isEmpty() -> annotationHelper.annotateReferenceToJson(fullKey)
                fullKey.ns == null && settings.suppressWarningsForUnresolvedDefaultNs && (fullKey.compositeKey.size == mostResolvedReference.unresolved.size) -> {}
                else -> annotationHelper.annotateUnresolved(fullKey, mostResolvedReference.path)
            }
        }
    }
}