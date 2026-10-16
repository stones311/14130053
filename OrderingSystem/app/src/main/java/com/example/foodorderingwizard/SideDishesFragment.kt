package com.example.foodorderingwizard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.foodorderingwizard.databinding.FragmentSideDishesBinding

class SideDishesFragment : Fragment() {

    private var _binding: FragmentSideDishesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSideDishesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val checkBoxesWithItems = listOf(
            binding.cbSideDish1 to viewModel.availableSideDishes[0],
            binding.cbSideDish2 to viewModel.availableSideDishes[1],
            binding.cbSideDish3 to viewModel.availableSideDishes[2],
            binding.cbSideDish4 to viewModel.availableSideDishes[3],
            binding.cbSideDish5 to viewModel.availableSideDishes[4]
        )

        for ((checkBox, sideDish) in checkBoxesWithItems) {
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleSideDish(sideDish, isChecked)
            }
        }

        viewModel.orderState.observe(viewLifecycleOwner) { state ->
            val selectedList = state.selectedSideDishes
            for ((checkBox, sideDish) in checkBoxesWithItems) {
                val shouldBeChecked = selectedList.contains(sideDish)
                if (checkBox.isChecked != shouldBeChecked) {
                    checkBox.isChecked = shouldBeChecked
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
