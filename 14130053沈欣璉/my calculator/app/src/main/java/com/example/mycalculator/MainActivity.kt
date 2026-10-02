package com.example.mycalculator

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : ComponentActivity() {

    // UI 元件宣告
    lateinit var tvInput: TextView
    lateinit var tvOldInput: TextView
    lateinit var tvCurrentOperand: TextView

    lateinit var btnOne: Button
    lateinit var btnTwo: Button
    lateinit var btnThree: Button
    lateinit var btnFour: Button
    lateinit var btnFive: Button
    lateinit var btnSix: Button
    lateinit var btnSeven: Button
    lateinit var btnEight: Button
    lateinit var btnNine: Button
    lateinit var btnZero: Button
    lateinit var btnDot: Button
    lateinit var btnPLus: Button
    lateinit var btnMinus: Button
    lateinit var btnMultiply: Button
    lateinit var btnDivide: Button
    lateinit var btnEqual: Button
    lateinit var clear: Button
    lateinit var allClear: Button
    lateinit var btnBackspace: ImageButton

    // 計算狀態變數
    var currentInput = StringBuilder()
    var currentOperator = Operator.NONE
    var operand1: BigDecimal? = null
    var isNewCalculation = false // 標記是否剛完成一次計算 (按下等號)

    // 重複等於與連續計算狀態
    var lastOperator = Operator.NONE
    var lastOperand2: BigDecimal? = null

    enum class Operator {
        NONE, ADD, SUBTRACT, MULTIPLY, DIVIDE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化 Views
        tvInput = findViewById(R.id.tvInput)
        tvOldInput = findViewById(R.id.tvOldInput)
        tvCurrentOperand = findViewById(R.id.tvCurrentOperand)

        btnOne = findViewById(R.id.btnOne)
        btnTwo = findViewById(R.id.btnTwo)
        btnThree = findViewById(R.id.btnThree)
        btnFour = findViewById(R.id.btnFour)
        btnFive = findViewById(R.id.btnFive)
        btnSix = findViewById(R.id.btnSix)
        btnSeven = findViewById(R.id.btnSeven)
        btnEight = findViewById(R.id.btnEight)
        btnNine = findViewById(R.id.btnNine)
        btnZero = findViewById(R.id.btnZero)
        btnDot = findViewById(R.id.btnDot)
        btnPLus = findViewById(R.id.btnPLus)
        btnMinus = findViewById(R.id.btnMinus)
        btnMultiply = findViewById(R.id.btnMultiply)
        btnDivide = findViewById(R.id.btnDivide)
        btnEqual = findViewById(R.id.btnEqual)
        clear = findViewById(R.id.clear)
        allClear = findViewById(R.id.allClear)
        btnBackspace = findViewById(R.id.btnBackspace)

        // 設定數字按鈕點擊事件
        btnOne.setOnClickListener { appendNumber("1") }
        btnTwo.setOnClickListener { appendNumber("2") }
        btnThree.setOnClickListener { appendNumber("3") }
        btnFour.setOnClickListener { appendNumber("4") }
        btnFive.setOnClickListener { appendNumber("5") }
        btnSix.setOnClickListener { appendNumber("6") }
        btnSeven.setOnClickListener { appendNumber("7") }
        btnEight.setOnClickListener { appendNumber("8") }
        btnNine.setOnClickListener { appendNumber("9") }
        btnZero.setOnClickListener { appendNumber("0") }
        btnDot.setOnClickListener { appendNumber(".") }

        // 設定運算符號按鈕點擊事件
        btnPLus.setOnClickListener { setOperator(Operator.ADD) }
        btnMinus.setOnClickListener { setOperator(Operator.SUBTRACT) }
        btnMultiply.setOnClickListener { setOperator(Operator.MULTIPLY) }
        btnDivide.setOnClickListener { setOperator(Operator.DIVIDE) }

        // 設定功能按鈕事件
        btnEqual.setOnClickListener { calculateResult() }
        clear.setOnClickListener { clearInput() }
        allClear.setOnClickListener { allClearInput() }
        btnBackspace.setOnClickListener { handleBackspace() }
    }

    private fun appendNumber(number: String) {
        // 如果剛按下等號算完結果，再按數字時自動開始全新的計算
        if (isNewCalculation) {
            allClearInput()
        }

        // 防呆：防止重複輸入小數點
        if (number == "." && currentInput.contains(".")) {
            return
        }

        currentInput.append(number)
        updateDisplay()
    }

    private fun handleBackspace() {
        if (isNewCalculation) return
        if (currentInput.isNotEmpty()) {
            currentInput.deleteCharAt(currentInput.length - 1)
            updateDisplay()
        }
    }

    private fun setOperator(operator: Operator) {
        val inputStr = currentInput.toString()

        if (operand1 == null) {
            operand1 = inputStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
            currentInput.clear()
        } else if (currentInput.isNotEmpty()) {
            // 連續運算支援（例如：輸入 5 + 3 再按 +，會自動先計算 5 + 3 = 8，再繼續設定 +）
            calculateResult()
            currentInput.clear()
        }

        // 重置重複等於狀態
        lastOperator = Operator.NONE
        lastOperand2 = null

        // 關鍵：準備進行下一階段運算，確保輸入數字時不會觸發清空
        isNewCalculation = false

        currentOperator = operator
        val opStr = operatorToString(operator)
        val displayOp1 = operand1?.stripTrailingZeros()?.toPlainString() ?: "0"

        tvOldInput.text = "$displayOp1 $opStr"
        tvCurrentOperand.text = opStr
        tvInput.text = displayOp1
    }

    private fun operatorToString(operator: Operator): String {
        return when (operator) {
            Operator.ADD -> "+"
            Operator.SUBTRACT -> "-"
            Operator.MULTIPLY -> "×"
            Operator.DIVIDE -> "÷"
            Operator.NONE -> ""
        }
    }

    private fun calculateResult() {
        // 判斷是否為重複按等於號 (例如: 5 + 3 = 8, 再按 = -> 11, 再按 = -> 14)
        val activeOperator = if (currentOperator != Operator.NONE) currentOperator else lastOperator
        val activeOperand1 = operand1 ?: BigDecimal.ZERO

        if (activeOperator == Operator.NONE) return

        val operand2: BigDecimal = if (currentInput.isNotEmpty()) {
            currentInput.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
        } else if (lastOperand2 != null && currentOperator == Operator.NONE) {
            lastOperand2!!
        } else {
            activeOperand1
        }

        var result: BigDecimal? = null

        Log.d("CalculatorApp", "activeOperand1= $activeOperand1, operand2= $operand2, activeOperator= $activeOperator")

        when (activeOperator) {
            Operator.ADD -> result = activeOperand1.add(operand2)
            Operator.SUBTRACT -> result = activeOperand1.subtract(operand2)
            Operator.MULTIPLY -> result = activeOperand1.multiply(operand2)
            Operator.DIVIDE -> {
                if (operand2.compareTo(BigDecimal.ZERO) != 0) {
                    result = activeOperand1.divide(operand2, 10, RoundingMode.HALF_UP)
                } else {
                    Log.e("CalculatorApp", "Division by zero attempted.")
                    tvInput.text = "Error: Div by 0"
                    currentInput.clear()
                    operand1 = null
                    currentOperator = Operator.NONE
                    lastOperator = Operator.NONE
                    lastOperand2 = null
                    tvOldInput.text = ""
                    tvCurrentOperand.text = ""
                    return
                }
            }
            Operator.NONE -> result = operand2
        }

        if (result != null) {
            val displayOp1 = activeOperand1.stripTrailingZeros().toPlainString()
            val displayOp2 = operand2.stripTrailingZeros().toPlainString()
            val opStr = operatorToString(activeOperator)

            tvOldInput.text = "$displayOp1 $opStr $displayOp2 ="
            tvInput.text = result.stripTrailingZeros().toPlainString()

            // 紀錄最後一次運算符與右運算子，支援重複按等於號
            lastOperator = activeOperator
            lastOperand2 = operand2

            operand1 = result
            currentInput.clear()
            currentOperator = Operator.NONE
            tvCurrentOperand.text = ""
            isNewCalculation = true
        }
    }

    private fun allClearInput() {
        currentInput.clear()
        operand1 = null
        currentOperator = Operator.NONE
        lastOperator = Operator.NONE
        lastOperand2 = null
        tvOldInput.text = ""
        tvInput.text = "0"
        tvCurrentOperand.text = ""
        isNewCalculation = false
    }

    private fun clearInput() {
        // C 按鈕：只清除當前正在輸入的數字，不清除已儲存的 operand1 與運算符
        currentInput.clear()
        tvInput.text = operand1?.stripTrailingZeros()?.toPlainString() ?: "0"
    }

    private fun updateDisplay() {
        tvInput.text = if (currentInput.isEmpty()) "0" else currentInput.toString()
    }
}