package com.example.foodorderingwizard

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.foodorderingwizard.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: OrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val adapter = WizardPagerAdapter(this)
        binding.viewPager.adapter = adapter

        val tabTitles = arrayOf("1. 主餐", "2. 副餐", "3. 飲料", "4. 確認")
        val stepSubtitles = arrayOf(
            "步驟 1/4：選擇美味主餐",
            "步驟 2/4：選擇精緻副餐",
            "步驟 3/4：選擇特調飲料與冰甜度",
            "步驟 4/4：確認訂單明細與送出"
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        val chips = listOf(
            binding.stepChip1,
            binding.stepChip2,
            binding.stepChip3,
            binding.stepChip4
        )

        chips.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                binding.viewPager.currentItem = index
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                binding.tvStepSubtitle.text = stepSubtitles[position]

                updateStepChips(position, chips)

                if (position == 0) {
                    binding.btnPrevious.visibility = View.INVISIBLE
                } else {
                    binding.btnPrevious.visibility = View.VISIBLE
                }

                if (position == adapter.itemCount - 1) {
                    binding.btnNext.visibility = View.GONE
                } else {
                    binding.btnNext.visibility = View.VISIBLE
                    binding.btnNext.text = "下一步"
                }
            }
        })

        binding.btnPrevious.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current > 0) {
                binding.viewPager.currentItem = current - 1
            }
        }

        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < adapter.itemCount - 1) {
                binding.viewPager.currentItem = current + 1
            }
        }

        viewModel.orderState.observe(this) { state ->
            binding.tvTotalPrice.text = "總金額: $${state.totalPrice}"
        }
    }

    private fun updateStepChips(activePosition: Int, chips: List<TextView>) {
        val typedValueOnPrimary = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValueOnPrimary, true)

        val typedValueOnContainer = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimaryContainer, typedValueOnContainer, true)

        chips.forEachIndexed { index, textView ->
            if (index == activePosition) {
                textView.setBackgroundResource(R.drawable.bg_step_chip_active)
                textView.setTextColor(typedValueOnPrimary.data)
            } else {
                textView.setBackgroundResource(R.drawable.bg_step_chip_inactive)
                textView.setTextColor(typedValueOnContainer.data)
            }
        }
    }

    fun navigateToPage(page: Int) {
        binding.viewPager.currentItem = page
    }
}
