package com.gbksoft.neighbourhood.mappers.base

object TimestampMapper {
    fun toServerTimestamp(time: Long) = time / 1000
    fun toAppTimestamp(time: Long) = time * 1000

    fun toServerTimestampOrNull(time: Long?) = if (time != null) toServerTimestamp(time) else null
    fun toAppTimestampOrNull(time: Long?) = if (time != null) toAppTimestamp(time) else null
}