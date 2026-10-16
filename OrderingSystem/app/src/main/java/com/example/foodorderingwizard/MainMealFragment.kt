package com.example.foodorderingwizard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.foodorderingwizard.databinding.FragmentMainMealBinding

class MainMealFragment : Fragment() {

    private var _binding: FragmentMainMealBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainMealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rgMainMeal.setOnCheckedChangeListener { _, checkedId ->
            val selectedMeal = when (checkedId) {
                R.id.rbMeal1 -> viewModel.mainMeals[0]
                R.id.rbMeal2 -> viewModel.mainMeals[1]
                R.id.rbMeal3 -> viewModel.mainMeals[2]
                R.id.rbMeal4 -> viewModel.mainMeals[3]
                R.id.rbMeal5 -> viewModel.mainMeals[4]
                else -> null
            }
            selectedMeal?.let { viewModel.selectMainMeal(it) }
        }

        viewModel.orderState.observe(viewLifecycleOwner) { state ->
            val meal = state.selectedMeal ?: return@observe
            val buttonId = when (viewModel.mainMeals.indexOf(meal)) {
                0 -> R.id.rbMeal1
                1 -> R.id.rbMeal2
                2 -> R.id.rbMeal3
                3 -> R.id.rbMeal4
                4 -> R.id.rbMeal5
                else -> -1
            }
            if (buttonId != -1 && binding.rgMainMeal.checkedRadioButtonId != buttonId) {
                binding.rgMainMeal.check(buttonId)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
