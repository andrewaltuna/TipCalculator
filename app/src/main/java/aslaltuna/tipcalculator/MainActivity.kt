package aslaltuna.tipcalculator

import android.animation.ArgbEvaluator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat

private const val TAG = "MainActivity"
private const val INITIAL_TIP_PERCENT = 15
private const val EMPTY_AMOUNT = "₱0.00"

class MainActivity : AppCompatActivity() {
    private lateinit var etBaseAmount: EditText
    private lateinit var seekBarTip: SeekBar
    private lateinit var tvTipPercent: TextView
    private lateinit var tvTipEmoji: TextView
    private lateinit var tvTipAmount: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvTipDescription: TextView
    private lateinit var switchUSD: Switch
    private lateinit var switchSplit: Switch
    private lateinit var etNumberOfPeople: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etBaseAmount = findViewById(R.id.etBaseAmount)
        seekBarTip = findViewById(R.id.seekBarTip)
        tvTipPercent = findViewById(R.id.tvTipPercent)
        tvTipEmoji = findViewById(R.id.tvTipEmoji)
        tvTipAmount = findViewById(R.id.tvTipAmount)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvTipDescription = findViewById(R.id.tvTipDescription)
        switchUSD = findViewById(R.id.switchUSD)
        switchSplit = findViewById(R.id.switchSplit)
        etNumberOfPeople = findViewById(R.id.etNumberOfPeople)

        seekBarTip.progress = INITIAL_TIP_PERCENT
        tvTipPercent.text = "$INITIAL_TIP_PERCENT%"
        updateTipDescription(INITIAL_TIP_PERCENT)
        seekBarTip.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                Log.i(TAG, "onProgressChanged $p1")
                tvTipPercent.text = "$p1%"
                computeTipAndTotal()
                updateTipDescription(p1)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        tvTipAmount.text = EMPTY_AMOUNT
        tvTotalAmount.text = EMPTY_AMOUNT
        etBaseAmount.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(p0: Editable?) {
                Log.i(TAG, "afterTextChanged $p0")
                computeTipAndTotal()
            }
        })

        switchUSD.setOnCheckedChangeListener(object: CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                val currency = if (p1) "$" else "₱"
                tvTipAmount.text = tvTipAmount.text.toString().replaceFirstChar { currency }
                tvTotalAmount.text = tvTotalAmount.text.toString().replaceFirstChar { currency }
//                tvTipAmount.text = currency + tvTipAmount.text
//                tvTotalAmount.text = currency + tvTotalAmount.text
            }
        })

        etNumberOfPeople.isFocusable = false
        switchSplit.setOnCheckedChangeListener(object: CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                computeTipAndTotal()
                if (p1) {
                    etNumberOfPeople.isFocusableInTouchMode = true
                } else {
                    etNumberOfPeople.isFocusable = false
                }
            }
        })

        etNumberOfPeople.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(p0: Editable?) {
                computeTipAndTotal()
            }

        })
    }

    private fun updateTipDescription(tipPercent: Int) {
        val tipDescription = when (tipPercent) {
            in 0..9 -> "Very Poor"
            in 10..14 -> "Poor"
            in 15..19 -> "Average"
            in 20..24 -> "Great"
            else -> "Amazing"
        }

        tvTipDescription.text = tipDescription

        // Update tip emoji

        val tipEmoji = when (tipPercent) {
            in 0..9 -> "😥"
            in 10..14 -> "🙁"
            in 15..19 -> "🙂"
            in 20..24 -> "😀"
            else -> "🥰"
        }

        tvTipEmoji.text = tipEmoji

        // Update text color based on percentage
        val color = ArgbEvaluator().evaluate(
            tipPercent.toFloat() / seekBarTip.max,
            ContextCompat.getColor(this, R.color.tip_worst),
            ContextCompat.getColor(this, R.color.tip_best)
        ) as Int

        tvTipDescription.setTextColor(color)
    }

    private fun computeTipAndTotal() {
        if (etBaseAmount.text.isEmpty()) {
            tvTipAmount.text = EMPTY_AMOUNT
            tvTotalAmount.text = EMPTY_AMOUNT
            return
        }

        // Get etBaseAmount and seekBarTip progress
        val baseAmount = etBaseAmount.text.toString().toDouble()
        val tipPercent = seekBarTip.progress

        // Compute tip and total
        var tipAmount = baseAmount * tipPercent / 100
        var totalAmount = baseAmount + tipAmount

        // If split enabled, divide tip and bill by number of people
        if (switchSplit.isChecked) {
            val numberOfPeople = if (etNumberOfPeople.text.isEmpty()) 1 else etNumberOfPeople.text.toString().toInt()

            tipAmount /= numberOfPeople
            totalAmount /= numberOfPeople
        }

        // Currency symbol
        val currency = if (switchUSD.isChecked) "$" else "₱"

        // Update UI
        tvTipAmount.text = "$currency%.2f".format(tipAmount)
        tvTotalAmount.text = "$currency%.2f".format(totalAmount)
    }
}