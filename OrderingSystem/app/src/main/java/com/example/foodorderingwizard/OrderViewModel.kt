package com.example.foodorderingwizard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OrderViewModel : ViewModel() {

    private val _orderState = MutableLiveData(OrderState())
    val orderState: LiveData<OrderState> = _orderState

    val mainMeals = listOf(
        MainMeal("經典美式牛肉堡", 130),
        MainMeal("勁辣雞腿堡", 120),
        MainMeal("黃金鱈魚堡", 110),
        MainMeal("奶油培根義大利麵", 140),
        MainMeal("日式炸豬排飯", 150)
    )

    val availableSideDishes = listOf(
        SideDish("美式黃金薯條", 45),
        SideDish("香酥雞塊 (6塊)", 55),
        SideDish("鮮蔬沙拉", 40),
        SideDish("葡式蛋塔", 35),
        SideDish("洋蔥圈", 45)
    )

    val availableDrinks = listOf(
        Drink("可口可樂", 35),
        Drink("冰檸檬紅茶", 30),
        Drink("經典奶茶", 40),
        Drink("翡翠綠茶", 30),
        Drink("美式咖啡", 50)
    )

    fun selectMainMeal(meal: MainMeal) {
        val current = _orderState.value ?: OrderState()
        _orderState.value = current.copy(selectedMeal = meal)
    }

    fun toggleSideDish(sideDish: SideDish, selected: Boolean) {
        val current = _orderState.value ?: OrderState()
        val currentList = current.selectedSideDishes.toMutableList()
        if (selected) {
            if (!currentList.contains(sideDish)) {
                currentList.add(sideDish)
            }
        } else {
            currentList.remove(sideDish)
        }
        _orderState.value = current.copy(selectedSideDishes = currentList)
    }

    fun selectDrink(drink: Drink) {
        val current = _orderState.value ?: OrderState()
        _orderState.value = current.copy(selectedDrink = drink)
    }

    fun setSugarLevel(sugar: SugarLevel) {
        val current = _orderState.value ?: OrderState()
        _orderState.value = current.copy(sugarLevel = sugar)
    }

    fun setIceLevel(ice: IceLevel) {
        val current = _orderState.value ?: OrderState()
        _orderState.value = current.copy(iceLevel = ice)
    }

    fun setCustomerNote(note: String) {
        val current = _orderState.value ?: OrderState()
        _orderState.value = current.copy(customerNote = note)
    }

    fun resetOrder() {
        _orderState.value = OrderState()
    }
}
