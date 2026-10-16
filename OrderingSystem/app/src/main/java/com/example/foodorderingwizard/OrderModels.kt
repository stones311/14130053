package com.example.foodorderingwizard

data class MainMeal(
    val name: String,
    val price: Int
)

data class SideDish(
    val name: String,
    val price: Int
)

data class Drink(
    val name: String,
    val price: Int
)

enum class SugarLevel(val displayName: String) {
    REGULAR("正常糖 (100%)"),
    LESS("少糖 (70%)"),
    HALF("半糖 (50%)"),
    MICRO("微糖 (30%)"),
    FREE("無糖 (0%)")
}

enum class IceLevel(val displayName: String) {
    REGULAR("正常冰"),
    LESS("少冰"),
    MICRO("微冰"),
    NO_ICE("去冰"),
    HOT("熱飲")
}

data class OrderState(
    val selectedMeal: MainMeal? = null,
    val selectedSideDishes: List<SideDish> = emptyList(),
    val selectedDrink: Drink? = null,
    val sugarLevel: SugarLevel = SugarLevel.REGULAR,
    val iceLevel: IceLevel = IceLevel.REGULAR,
    val customerNote: String = ""
) {
    val totalPrice: Int
        get() {
            var total = selectedMeal?.price ?: 0
            total += selectedSideDishes.sumOf { it.price }
            total += selectedDrink?.price ?: 0
            return total
        }
}
