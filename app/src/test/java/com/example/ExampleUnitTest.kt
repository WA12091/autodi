package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCallDispositionsEnum() {
    val dispositions = com.example.data.model.CallDisposition.entries
    assertTrue(dispositions.isNotEmpty())
    assertEquals(com.example.data.model.CallDisposition.CONNECTED, com.example.data.model.CallDisposition.fromString("CONNECTED"))
    assertEquals(com.example.data.model.CallDisposition.CONVERTED, com.example.data.model.CallDisposition.fromString("CONVERTED"))
    assertEquals(com.example.data.model.CallDisposition.VOICEMAIL, com.example.data.model.CallDisposition.fromString("VOICEMAIL"))
  }
}
