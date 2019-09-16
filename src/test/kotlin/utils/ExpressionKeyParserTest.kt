package utils

import com.eny.i18n.plugin.utils.*
import org.junit.Test
import kotlin.test.assertEquals

class ExpressionKeyParserTest : TestBase {

//fileName:ROOT.Key2.Key3                   /                       / fileName{8}:ROOT{4}.Key2{4}.Key3{4}
    @Test
    fun parseSimpleLiteral() {
        val literal = listOf(
                KeyElement.fromLiteral("fileName:ROOT.Key2.Key3")
        )
        val parser = ExpressionKeyParser()
        val expected = FullKey(
                listOf(Literal("fileName")),
                listOf(
                        Literal("ROOT"),
                        Literal("Key2"),
                        Literal("Key3")
                )
        )
        val actual = parser.parse(literal)
        val textLengths = extractLengths(actual?.compositeKey)
        assertEquals(expected, actual)
        assertEquals(23, actual?.length)
        assertEquals(8, actual?.nsLength)
        assertEquals(14, actual?.keyLength)
        assertEquals(listOf(4, 4, 4), textLengths)
    }

//${fileExpr}:ROOT.Key1.Key31               / sample                / sample{11}:ROOT{4}.Key1{4}.Key31{5}
    @Test
    fun parseExpressionWithFilePartInTemplate() {
        val elements = listOf(
            KeyElement("\${fileExpr}", "sample", KeyElementType.TEMPLATE),
            KeyElement(":ROOT.Key1.Key31", ":ROOT.Key1.Key31", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("sample")
        val expectedKey = listOf("ROOT", "Key1", "Key31")
        val parsed = parser.parse(elements)
        val textLengths = extractLengths(parsed?.compositeKey)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
        assertEquals(27, parsed?.length)
        assertEquals(11, parsed?.nsLength)
        assertEquals(15, parsed?.keyLength)
        assertEquals(listOf(4, 4, 5), textLengths)
    }

//prefix${fileExpr}:ROOT.Key4.Key5          / sample                / prefixsample{11}:ROOT{4}.Key4{4}.Key5{4}
    @Test
    fun parsePrefixedExpressionWithFilePartInTemplate() {
        val elements = listOf(
                KeyElement("prefix", "prefix", KeyElementType.LITERAL),
                KeyElement("\${fileExpr}", "sample", KeyElementType.TEMPLATE),
                KeyElement(":ROOT.Key4.Key5", ":ROOT.Key4.Key5", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("prefixsample")
        val expectedKey = listOf("ROOT", "Key4", "Key5")
        val parsed = parser.parse(elements)
        val textLengths = extractLengths(parsed?.compositeKey)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
        assertEquals(17, parsed?.nsLength)
        assertEquals(14, parsed?.keyLength)
        assertEquals(32, parsed?.length)
        assertEquals(listOf(4, 4, 4), textLengths)
    }

//${fileExpr}postfix:ROOT.Key4.Key5         / sample                / samplepostfix{18}:ROOT{4}.Key4{4}.Key5{4}
//prefix${fileExpr}postfix:ROOT.Key4.Key5   / sample                / prefixsamplepostfix{24}:ROOT{4}.Key4{4}.Key5{4}
//prefix${fileExpr}postfix.ROOT.Key4.Key5   / partFile:partKey      / prefixpartFile{17}:partKeypostfix{6}.ROOT{4}.Key4{4}.Key5{4}
//filename:${key}                           / Key0.Key2.Key21       / filename{8}:Key0{5}.Key2{0}.Key21{0}
//filename:${key}item                       / Key0.Key2.Key21.      / filename{8}:Key0{5}.Key2{0}.Key21{0}.item{3}
//filename:${key}.item                      / Key0.Key2.Key21       / filename{8}:Key0{5}.Key2{0}.Key21{0}.item{4}
//filename:${key}item                       / Key0.Key2.Key21       / filename{8}:Key0{5}.Key2{0}.Key21item{3}
//filename:root.${key}                      / Key0.Key2.Key21       / filename{8}:root{4}.Key0{5}.Key2{0}.Key21{0}
//filename:root${key}                       / .Key0.Key2.Key21      / filename{8}:root{4}.Key0{4}.Key2{0}.Key21{0}
//filename:root${key}                       / Key0.Key2.Key21       / filename{8}:rootKey0{9}.Key2{0}.Key21{0}
    @Test
    fun parsePostfixedExpressionWithFilePartInTemplate() {
        val elements = listOf(
            KeyElement("\${fileExpr}", "sample", KeyElementType.TEMPLATE),
            KeyElement("postfix", "postfix", KeyElementType.LITERAL),
            KeyElement(":ROOT.Key4.Key5", ":ROOT.Key4.Key5", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("samplepostfix")
        val expectedKey = listOf("ROOT", "Key4", "Key5")
        val parsed = parser.parse(elements)
        val textLengths = extractLengths(parsed?.compositeKey)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
        assertEquals(18, parsed?.nsLength)
        assertEquals(14, parsed?.keyLength)
        assertEquals(33, parsed?.length)
        assertEquals(listOf(4, 4, 4), textLengths)
    }

    //prefix${fileExpr}postfix:ROOT.Key4.Key5  / sample
//    @Test
    fun parseMixedExpressionWithFilePartInTemplate() {
        val elements = listOf(
                KeyElement("prefix", "prefix", KeyElementType.LITERAL),
                KeyElement("\${fileExpr}", "sample", KeyElementType.TEMPLATE),
                KeyElement("postfix", "postfix", KeyElementType.LITERAL),
                KeyElement(":ROOT.Key4.Key5", ":ROOT.Key4.Key5", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("prefixsamplepostfix")
        val expectedKey = listOf("ROOT", "Key4", "Key5")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

    //prefix${fileExpr}postfix.ROOT.Key4.Key5   / partFile:partKey
//    @Test
    fun parseNsSeparatorInExpression() {
        val elements = listOf(
                KeyElement("prefix", "prefix", KeyElementType.LITERAL),
                KeyElement("\${fileExpr}", "partFile:partKey", KeyElementType.TEMPLATE),
                KeyElement("postfix", "postfix", KeyElementType.LITERAL),
                KeyElement(".ROOT.Key4.Key5", ".ROOT.Key4.Key5", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("prefixpartFile")
        val expectedKey = listOf("partKeypostfix", "ROOT", "Key4", "Key5")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

    //filename:${key}   / Key0.Key2.Key21
//    @Test
    fun parseExpressionWithKeyInTemplate() {
        val elements = listOf(
            KeyElement("filename:", "filename:", KeyElementType.LITERAL),
            KeyElement("\${key}", "Key0.Key2.Key21", KeyElementType.TEMPLATE)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("filename")
        val expectedKey = listOf("Key0", "Key2", "Key21")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

//    filename:${key}item   / Key0.Key2.Key21.
//    @Test
    fun parseExpressionWithKeyInTemplate2() {
        val elements = listOf(
                KeyElement("filename:", "filename:", KeyElementType.LITERAL),
                KeyElement("\${key}", "Key0.Key2.Key21.", KeyElementType.TEMPLATE),
                KeyElement("item", "item", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("filename")
        val expectedKey = listOf("Key0", "Key2", "Key21", "item")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

    //filename:${key}.item   / Key0.Key2.Key21
//    @Test
    fun parseExpressionWithKeyInTemplate3() {
        val elements = listOf(
                KeyElement("filename:", "filename:", KeyElementType.LITERAL),
                KeyElement("\${key}", "Key0.Key2.Key21", KeyElementType.TEMPLATE),
                KeyElement(".item", ".item", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("filename")
        val expectedKey = listOf("Key0", "Key2", "Key21", "item")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

    //filename:${key}item   / Key0.Key2.Key21
//    @Test
    fun parseExpressionWithKeyInTemplate4() {
        val elements = listOf(
                KeyElement("filename:", "filename:", KeyElementType.LITERAL),
                KeyElement("\${key}", "Key0.Key2.Key21", KeyElementType.TEMPLATE),
                KeyElement("item", "item", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("filename")
        val expectedKey = listOf("Key0", "Key2", "Key21item")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

    //filename:root.${key}  / Key0.Key2.Key21
//    @Test
    fun partOfKeyIsExpression() {
        val elements = listOf(
            KeyElement("filename:root.", "filename:root.", KeyElementType.LITERAL),
            KeyElement("\${key}", "Key0.Key2.Key21", KeyElementType.TEMPLATE)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("filename")
        val expectedKey = listOf("root", "Key0", "Key2", "Key21")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

    //filename:root${key}   / .Key0.Key2.Key21
//    @Test
    fun partOfKeyIsExpression2() {
        val elements = listOf(
                KeyElement("filename:root", "filename:root", KeyElementType.LITERAL),
                KeyElement("\${key}", ".Key0.Key2.Key21", KeyElementType.TEMPLATE)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("filename")
        val expectedKey = listOf("root", "Key0", "Key2", "Key21")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

    //filename:root${key}   / Key0.Key2.Key21
//    @Test
    fun partOfKeyIsExpression3() {
        val elements = listOf(
                KeyElement("filename:root", "filename:root", KeyElementType.LITERAL),
                KeyElement("\${key}", "Key0.Key2.Key21", KeyElementType.TEMPLATE)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("filename")
        val expectedKey = listOf("rootKey0", "Key2", "Key21")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }
}

//fileName:ROOT.Key2.Key3                   /                       / fileName{8}:ROOT{4}.Key2{4}.Key3{4}
//${fileExpr}:ROOT.Key1.Key31               / sample                / sample{11}:ROOT{4}.Key1{4}.Key31{5}
//prefix${fileExpr}:ROOT.Key4.Key5          / sample                / prefixsample{11}:ROOT{4}.Key4{4}.Key5{4}
//${fileExpr}postfix:ROOT.Key4.Key5         / sample                / samplepostfix{18}:ROOT{4}.Key4{4}.Key5{4}
//prefix${fileExpr}postfix:ROOT.Key4.Key5   / sample                / prefixsamplepostfix{24}:ROOT{4}.Key4{4}.Key5{4}
//prefix${fileExpr}postfix.ROOT.Key4.Key5   / partFile:partKey      / prefixpartFile{17}:partKeypostfix{6}.ROOT{4}.Key4{4}.Key5{4}
//filename:${key}                           / Key0.Key2.Key21       / filename{8}:Key0{5}.Key2{0}.Key21{0}
//filename:${key}item                       / Key0.Key2.Key21.      / filename{8}:Key0{5}.Key2{0}.Key21{0}.item{3}
//filename:${key}.item                      / Key0.Key2.Key21       / filename{8}:Key0{5}.Key2{0}.Key21{0}.item{4}
//filename:${key}item                       / Key0.Key2.Key21       / filename{8}:Key0{5}.Key2{0}.Key21item{3}
//filename:root.${key}                      / Key0.Key2.Key21       / filename{8}:root{4}.Key0{5}.Key2{0}.Key21{0}
//filename:root${key}                       / .Key0.Key2.Key21      / filename{8}:root{4}.Key0{4}.Key2{0}.Key21{0}
//filename:root${key}                       / Key0.Key2.Key21       / filename{8}:rootKey0{9}.Key2{0}.Key21{0}
