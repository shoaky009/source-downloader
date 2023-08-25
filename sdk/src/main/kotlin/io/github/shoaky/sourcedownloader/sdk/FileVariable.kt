package io.github.shoaky.sourcedownloader.sdk

interface FileVariable {

    /**
     * @return 模版变量
     */
    fun patternVariables(): PatternVariables

    companion object {

        @JvmStatic
        val EMPTY: FileVariable = EmptyFileVariable
    }
}

private object EmptyFileVariable : FileVariable {

    override fun patternVariables(): PatternVariables = PatternVariables.EMPTY

}