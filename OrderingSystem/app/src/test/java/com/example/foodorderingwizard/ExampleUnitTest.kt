package com.example.foodorderingwizard

import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun testOrderStateTotalPrice() {
        val meal = MainMeal("經典美式牛肉堡", 130)
        val side1 = SideDish("美式黃金薯條", 45)
        val side2 = SideDish("香酥雞塊 (6塊)", 55)
        val drink = Drink("可口可樂", 35)

        val state = OrderState(
            selectedMeal = meal,
            selectedSideDishes = listOf(side1, side2),
            selectedDrink = drink,
            sugarLevel = SugarLevel.LESS,
            iceLevel = IceLevel.NO_ICE
        )

        val expectedTotal = 130 + 45 + 55 + 35
        assertEquals(expectedTotal, state.totalPrice)
    }

    @Test
    fun testEmptyOrderStateTotalPrice() {
        val state = OrderState()
        assertEquals(0, state.totalPrice)
    }
}
