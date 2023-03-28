package xyz.shoaky.sourcedownloader.core.component


// class JavaScriptVariableProvider(private val script: String) : VariableProvider {
//     override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
//         return ScriptFileGroup(sourceItem, script)
//     }
//
//     override fun support(item: SourceItem): Boolean = true
//
// }
//
// private class ScriptFileGroup(
//     val sourceItem: SourceItem,
//     val script: String
// ) : SourceItemGroup {
//     override fun sourceFiles(paths: List<Path>): List<SourceFile> {
//         return paths.map {
//             ScriptFile(it, sourceItem, script)
//         }
//     }
// }
//
// class ScriptFile(
//     private val path: Path,
//     private val sourceItem: SourceItem,
//     private val script: String
// ) : SourceFile {
//
//     private val vars: PatternVariables by lazy {
//         eval()
//     }
//
//     override fun patternVariables(): PatternVariables {
//         return vars
//     }
//
//     private fun eval(): PatternVariables {
//         val bindings = SimpleBindings()
//         bindings["path"] = path.toString()
//         bindings["item"] = sourceItem
//         val vars = mutableMapOf<String, String>()
//         bindings["vars"] = vars
//         ScriptEngine.jsEngine.eval(script, bindings)
//         return MapPatternVariables(vars)
//     }
// }
//
// object ScriptVariableProviderSupplier : SdComponentSupplier<JavaScriptVariableProvider> {
//     override fun apply(props: ComponentProps): JavaScriptVariableProvider {
//         return JavaScriptVariableProvider(props.get("script"))
//     }
//
//     override fun supplyTypes(): List<ComponentType> {
//         return listOf(
//             ComponentType.provider("script"),
//             ComponentType.provider("js"),
//             ComponentType.provider("javascript"),
//         )
//     }
//
//     override fun getComponentClass(): Class<JavaScriptVariableProvider> {
//         return JavaScriptVariableProvider::class.java
//     }
//
// }