package com.example.mycalculator

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.mycalculator.databinding.ActivityMainBinding
import java.util.Locale

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var firstOperand: Double? = null
    private var pendingOperator: String? = null
    private var isNewInput: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 所有數字按鈕
        val numberButtons = listOf(
            binding.btnZero, binding.btnOne, binding.btnTwo, binding.btnThree,
            binding.btnFour, binding.btnFive, binding.btnSix, binding.btnSeven,
            binding.btnEight, binding.btnNine
        )
        numberButtons.forEach { button ->
            button.setOnClickListener { appendNumber(button.text.toString()) }
        }

        // 小數點:只允許出現一次
        binding.btnDot.setOnClickListener { appendDot() }

        // 運算符號按鈕 (+ - × ÷)
        binding.btnPLus.setOnClickListener { setOperator("+") }
        binding.btnMinus.setOnClickListener { setOperator("-") }
        binding.btnMultiply.setOnClickListener { setOperator("×") }
        binding.btnDivide.setOnClickListener { setOperator("÷") }

        // 等號 (=)
        binding.btnEqual.setOnClickListener { calculateResult() }

        // AC: 完全清除
        binding.allClear.setOnClickListener { clearAll() }

        // C: 清除當前輸入
        binding.clear.setOnClickListener { clearCurrent() }

        // 退格: 刪掉最後一個字元
        binding.btnBackspace.setOnClickListener { backspace() }
    }

    private fun appendNumber(number: String) {
        val current = binding.tvInput.text.toString()
        if (isNewInput || current == "0" || current == "Error") {
            binding.tvInput.text = number
            isNewInput = false
        } else {
            binding.tvInput.append(number)
        }
    }

    private fun appendDot() {
        val current = binding.tvInput.text.toString()
        if (isNewInput || current == "Error") {
            binding.tvInput.text = "0."
            isNewInput = false
        } else if (!current.contains(".")) {
            binding.tvInput.append(".")
        }
    }

    private fun setOperator(operator: String) {
        val currentVal = binding.tvInput.text.toString().toDoubleOrNull() ?: return

        if (firstOperand != null && pendingOperator != null && !isNewInput) {
            val result = performCalculation(firstOperand!!, currentVal, pendingOperator!!)
            firstOperand = result
            binding.tvInput.text = formatResult(result)
        } else {
            firstOperand = currentVal
        }

        pendingOperator = operator
        binding.tvCurrentOperand.text = operator
        binding.tvOldInput.text = "${formatResult(firstOperand!!)} $operator"
        isNewInput = true
    }

    private fun calculateResult() {
        val operator = pendingOperator ?: return
        val first = firstOperand ?: return
        val second = binding.tvInput.text.toString().toDoubleOrNull() ?: return

        val result = performCalculation(first, second, operator)
        val formattedFirst = formatResult(first)
        val formattedSecond = formatResult(second)

        binding.tvOldInput.text = "$formattedFirst $operator $formattedSecond ="
        binding.tvInput.text = formatResult(result)
        binding.tvCurrentOperand.text = ""

        firstOperand = if (result.isNaN() || result.isInfinite()) null else result
        pendingOperator = null
        isNewInput = true
    }

    private fun performCalculation(first: Double, second: Double, operator: String): Double {
        return when (operator) {
            "+" -> first + second
            "-" -> first - second
            "×" -> first * second
            "÷" -> if (second != 0.0) first / second else Double.NaN
            else -> second
        }
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.8f", value).trimEnd('0').trimEnd('.')
        }
    }

    private fun clearAll() {
        binding.tvInput.text = "0"
        binding.tvOldInput.text = ""
        binding.tvCurrentOperand.text = ""
        firstOperand = null
        pendingOperator = null
        isNewInput = false
    }

    private fun clearCurrent() {
        binding.tvInput.text = "0"
    }

    private fun backspace() {
        if (isNewInput) return
        val current = binding.tvInput.text.toString()
        if (current == "Error") {
            binding.tvInput.text = "0"
            return
        }
        val next = if (current.length > 1) current.dropLast(1) else "0"
        binding.tvInput.text = if (next == "-" || next.isEmpty()) "0" else next
    }
}