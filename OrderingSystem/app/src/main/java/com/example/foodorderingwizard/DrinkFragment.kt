package com.example.foodorderingwizard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.foodorderingwizard.databinding.FragmentDrinkBinding

class DrinkFragment : Fragment() {

    private var _binding: FragmentDrinkBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrinkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rgDrink.setOnCheckedChangeListener { _, checkedId ->
            val drink = when (checkedId) {
                R.id.rbDrink1 -> viewModel.availableDrinks[0]
                R.id.rbDrink2 -> viewModel.availableDrinks[1]
                R.id.rbDrink3 -> viewModel.availableDrinks[2]
                R.id.rbDrink4 -> viewModel.availableDrinks[3]
                R.id.rbDrink5 -> viewModel.availableDrinks[4]
                else -> null
            }
            drink?.let { viewModel.selectDrink(it) }
        }

        binding.rgSugar.setOnCheckedChangeListener { _, checkedId ->
            val sugar = when (checkedId) {
                R.id.rbSugarRegular -> SugarLevel.REGULAR
                R.id.rbSugarLess -> SugarLevel.LESS
                R.id.rbSugarHalf -> SugarLevel.HALF
                R.id.rbSugarMicro -> SugarLevel.MICRO
                R.id.rbSugarFree -> SugarLevel.FREE
                else -> SugarLevel.REGULAR
            }
            viewModel.setSugarLevel(sugar)
        }

        binding.rgIce.setOnCheckedChangeListener { _, checkedId ->
            val ice = when (checkedId) {
                R.id.rbIceRegular -> IceLevel.REGULAR
                R.id.rbIceLess -> IceLevel.LESS
                R.id.rbIceMicro -> IceLevel.MICRO
                R.id.rbIceNone -> IceLevel.NO_ICE
                R.id.rbIceHot -> IceLevel.HOT
                else -> IceLevel.REGULAR
            }
            viewModel.setIceLevel(ice)
        }

        viewModel.orderState.observe(viewLifecycleOwner) { state ->
            val drink = state.selectedDrink
            if (drink != null) {
                val drinkBtnId = when (viewModel.availableDrinks.indexOf(drink)) {
                    0 -> R.id.rbDrink1
                    1 -> R.id.rbDrink2
                    2 -> R.id.rbDrink3
                    3 -> R.id.rbDrink4
                    4 -> R.id.rbDrink5
                    else -> -1
                }
                if (drinkBtnId != -1 && binding.rgDrink.checkedRadioButtonId != drinkBtnId) {
                    binding.rgDrink.check(drinkBtnId)
                }
            }

            val sugarBtnId = when (state.sugarLevel) {
                SugarLevel.REGULAR -> R.id.rbSugarRegular
                SugarLevel.LESS -> R.id.rbSugarLess
                SugarLevel.HALF -> R.id.rbSugarHalf
                SugarLevel.MICRO -> R.id.rbSugarMicro
                SugarLevel.FREE -> R.id.rbSugarFree
            }
            if (binding.rgSugar.checkedRadioButtonId != sugarBtnId) {
                binding.rgSugar.check(sugarBtnId)
            }

            val iceBtnId = when (state.iceLevel) {
                IceLevel.REGULAR -> R.id.rbIceRegular
                IceLevel.LESS -> R.id.rbIceLess
                IceLevel.MICRO -> R.id.rbIceMicro
                IceLevel.NO_ICE -> R.id.rbIceNone
                IceLevel.HOT -> R.id.rbIceHot
            }
            if (binding.rgIce.checkedRadioButtonId != iceBtnId) {
                binding.rgIce.check(iceBtnId)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
