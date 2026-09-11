package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AutoDialerDatabase
import com.example.data.model.CampaignSchedule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AutoDialer Pro", appName)
  }

  @Test
  fun `verify dispositions titles`() {
    assertEquals("Connected", com.example.data.model.CallDisposition.CONNECTED.title)
    assertEquals("Converted / Sale", com.example.data.model.CallDisposition.CONVERTED.title)
    assertEquals("Left Voicemail", com.example.data.model.CallDisposition.VOICEMAIL.title)
  }

  @Test
  fun `verify campaign schedule persistence and dao operations`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AutoDialerDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val dao = db.campaignScheduleDao()

    val now = System.currentTimeMillis()
    val schedule = CampaignSchedule(
      campaignId = 10,
      campaignName = "Enterprise Outbound",
      title = "VIP Executive Call Sprint",
      scheduledTimeMillis = now + 10000,
      repeatOption = "DAILY",
      status = "SCHEDULED",
      notes = "Focus on enterprise SLA features"
    )

    val id = dao.insertSchedule(schedule)
    assertNotNull(id)

    val pending = dao.getPendingSchedules().first()
    assertEquals(1, pending.size)
    assertEquals("VIP Executive Call Sprint", pending[0].title)
    assertEquals("DAILY", pending[0].repeatOption)

    // Test updating status
    dao.updateScheduleStatus(id, "COMPLETED")
    val updatedPending = dao.getPendingSchedules().first()
    assertEquals(0, updatedPending.size)

    val all = dao.getAllSchedules().first()
    assertEquals(1, all.size)
    assertEquals("COMPLETED", all[0].status)

    db.close()
  }
}
