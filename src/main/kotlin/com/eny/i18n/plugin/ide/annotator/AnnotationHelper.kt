package com.eny.i18n.plugin.ide.annotator

import com.eny.i18n.plugin.factory.TranslationFolderSelector
import com.eny.i18n.plugin.ide.quickfix.*
import com.eny.i18n.plugin.ide.settings.Settings
import com.eny.i18n.plugin.key.FullKey
import com.eny.i18n.plugin.key.lexer.Literal
import com.eny.i18n.plugin.tree.PropertyReference
import com.eny.i18n.plugin.utils.PluginBundle
import com.eny.i18n.plugin.utils.RangesCalculator
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

/**
 * Annotation helper methods
 */
class AnnotationHelper(private val holder: AnnotationHolder, private val rangesCalculator: RangesCalculator, private val project: Project, private val folderSelector: TranslationFolderSelector) {

    private val RESOLVED_COLOR = DefaultLanguageHighlighterColors.LINE_COMMENT
    private val errorSeverity = HighlightSeverity.WARNING
    private val infoSeverity = HighlightSeverity.INFORMATION

    /**
     * Annotates resolved translation key
     */
    fun annotateResolved(fullKey: FullKey) {
        holder.newAnnotation(infoSeverity, "")
            .range(rangesCalculator.compositeKeyFullBounds(fullKey))
            .textAttributes(RESOLVED_COLOR)
            .create()
    }

    /**
     * Annotates reference to object, not a leaf key in json/yaml
     */
    fun annotateReferenceToObject(fullKey: FullKey) {
        holder.newAnnotation(errorSeverity, PluginBundle.getMessage("annotator.object.reference"))
            .range(rangesCalculator.compositeKeyFullBounds(fullKey))
            .create()
    }

    /**
     * Annotates unresolved namespace
     */
    fun unresolvedNs(fullKey: FullKey, ns: Literal) {
        Settings.getInstance(project).mainFactory().contentGenerators().forEach {
            holder.newAnnotation(errorSeverity, PluginBundle.getMessage("annotator.unresolved.ns"))
                .range(rangesCalculator.unresolvedNs(fullKey))
                .withFix(CreateTranslationFileQuickFix(fullKey, it, folderSelector, ns.text))
                .create()
        }
    }

    /**
     * Annotates unresolved default namespace
     */
    fun unresolvedDefaultNs(fullKey: FullKey) {
        holder.newAnnotation(errorSeverity, PluginBundle.getMessage("annotator.missing.default.ns"))
            .range(rangesCalculator.compositeKeyFullBounds(fullKey))
            .create()
    }

    /**
     * Annotates unresolved composite key
     */
    fun unresolvedKey(fullKey: FullKey, mostResolvedReference: PropertyReference<PsiElement>) {
        val generators = Settings.getInstance(project).mainFactory().contentGenerators()
        holder.newAnnotation(errorSeverity, PluginBundle.getMessage("annotator.unresolved.key"))
            .withFix(CreateKeyQuickFix(fullKey, UserChoice(), PluginBundle.getMessage("quickfix.create.key"), generators))
            .withFix(CreateKeyQuickFix(fullKey, AllSourcesSelector(), PluginBundle.getMessage("quickfix.create.key.in.files"), generators))
            .range(rangesCalculator.unresolvedKey(fullKey, mostResolvedReference.path))
            .create()
    }

    /**
     * Annotates partially translated key and creates quick fix for it.
     */
    fun annotatePartiallyTranslated(fullKey: FullKey, references: List<PropertyReference<PsiElement>>) {
        val minimalResolvedReference = references.minBy { it.path.size }
        holder.newAnnotation(errorSeverity, PluginBundle.getMessage("annotator.partially.translated"))
            .range(rangesCalculator.unresolvedKey(fullKey, minimalResolvedReference.path))
            .withFix(CreateMissingKeysQuickFix(fullKey, Settings.getInstance(project).mainFactory(), references, PluginBundle.getMessage("quickfix.create.missing.keys")))
            .create()
    }
}