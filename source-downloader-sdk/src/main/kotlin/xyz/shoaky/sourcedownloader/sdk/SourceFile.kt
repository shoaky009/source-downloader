package xyz.shoaky.sourcedownloader.sdk

interface SourceFile {

    /**
     * @return 模版变量
     */
    fun patternVariables(): PatternVariables

    companion object {
        @JvmStatic
        val EMPTY: SourceFile = EmptySourceFile
    }
}

private object EmptySourceFile : SourceFile {
    override fun patternVariables(): PatternVariables = PatternVariables.EMPTY

}