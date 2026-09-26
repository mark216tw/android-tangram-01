package com.example.minitangram

import org.junit.Assert.assertEquals
import org.junit.Test

class CompletionRecordMessageTest {
    @Test
    fun firstCompletionIsANewBestRecord() {
        assertEquals("新的最佳紀錄", completionRecordMessage(42, null))
    }

    @Test
    fun fasterCompletionComparesWithPreviousBestRecord() {
        assertEquals("比最佳紀錄快了 8 秒", completionRecordMessage(42, 50))
    }

    @Test
    fun slowerCompletionComparesWithBestRecord() {
        assertEquals("比最佳紀錄慢了 8 秒", completionRecordMessage(50, 42))
    }

    @Test
    fun equalCompletionMatchesBestRecord() {
        assertEquals("與最佳紀錄相同", completionRecordMessage(42, 42))
    }
}
