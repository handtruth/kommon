package com.handtruth.kommon

class LogFactory(
    val tag: String = getDefaultTag(),
    val level: LogLevel = LogLevel.Info,
    val creator: (String, LogLevel) -> Log = Log.Key::default
) {
    fun log() = creator(tag, level)
    fun log(subTag: String) = creator("$tag/$subTag", level)
    fun factory(subTag: String) = LogFactory("$tag/$subTag", level, creator)
}
