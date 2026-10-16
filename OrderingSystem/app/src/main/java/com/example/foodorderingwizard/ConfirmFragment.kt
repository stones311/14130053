package com.example.foodorderingwizard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.foodorderingwizard.databinding.FragmentConfirmBinding

class ConfirmFragment : Fragment() {

    private var _binding: FragmentConfirmBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setCustomerNote(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.orderState.observe(viewLifecycleOwner) { state ->
            binding.tvMainMeal.text = state.selectedMeal?.let { "${it.name} ($${it.price})" } ?: "未選擇"

            if (state.selectedSideDishes.isEmpty()) {
                binding.tvSideDishes.text = "無"
            } else {
                binding.tvSideDishes.text = state.selectedSideDishes.joinToString("\n") {
                    "• ${it.name} ($${it.price})"
                }
            }

            if (state.selectedDrink != null) {
                binding.tvDrink.text = "${state.selectedDrink.name} ($${state.selectedDrink.price})\n(${state.sugarLevel.displayName} / ${state.iceLevel.displayName})"
            } else {
                binding.tvDrink.text = "未選擇"
            }

            binding.tvTotalPriceConfirm.text = "應付總金額：$${state.totalPrice}"
        }

        binding.btnSubmitOrder.setOnClickListener {
            val state = viewModel.orderState.value ?: return@setOnClickListener

            if (state.selectedMeal == null) {
                Toast.makeText(requireContext(), "請至少選擇一項主餐！", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(requireContext())
                .setTitle("訂單確認")
                .setMessage("訂單已成功送出！\n總金額：$${state.totalPrice}\n感謝您的光臨！")
                .setPositiveButton("確定") { dialog, _ ->
                    dialog.dismiss()
                    viewModel.resetOrder()
                    binding.etNote.setText("")
                    (activity as? MainActivity)?.navigateToPage(0)
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
