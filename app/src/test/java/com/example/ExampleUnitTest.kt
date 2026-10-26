package com.example

import com.example.model.LevelFormula
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testLevelFormulaProgression() {
    val reqLevel1 = LevelFormula.xpRequiredForLevel(1)
    assertEquals(150, reqLevel1)

    val reqLevel2 = LevelFormula.xpRequiredForLevel(2)
    assertEquals(200, reqLevel2)

    val prog = LevelFormula.calculateProgression(
      currentLevel = 1,
      currentXp = 50,
      xpEarned = 120
    )
    assertTrue(prog.didLevelUp)
    assertEquals(2, prog.newLevel)
    assertEquals(20, prog.newXp)
  }

  @Test
  fun testLevelTitles() {
    assertEquals("Beginner", LevelFormula.getLevelTitle(1))
    assertEquals("Quick Thinker", LevelFormula.getLevelTitle(2))
    assertEquals("Brain Explorer", LevelFormula.getLevelTitle(3))
    assertEquals("Mind Master", LevelFormula.getLevelTitle(4))
    assertEquals("Brain Champion", LevelFormula.getLevelTitle(5))
  }
}
