package com.handtruth.kommon

internal class LogAppender(private val log: Log, private val level: LogLevel) : Appendable {
    private val data = StringBuilder()

    override fun append(value: Char): Appendable {
        if (value == '\n') {
            log.internalWrite(level, data.toString())
            data.clear()
        } else {
            data.append(value)
        }
        return this
    }

    override fun append(value: CharSequence?) = append(value, 0, value?.length ?: 0)

    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable {
        value ?: return this
        var oldPos = startIndex
        while (true) {
            val pos = value.indexOf(startIndex = oldPos, char = '\n').let { if (it >= endIndex) -1 else it }
            if (pos == -1) {
                data.append(value.subSequence(oldPos, endIndex))
                break
            } else {
                data.append(value.subSequence(oldPos, pos))
                log.internalWrite(level, data.toString())
                data.clear()
                oldPos = pos + 1
            }
        }
        return this
    }
}

internal object VoidAppender : Appendable {
    override fun append(value: Char): Appendable = this
    override fun append(value: CharSequence?): Appendable = this
    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable = this
}
