package aslaltuna.tipcalculator

import android.animation.ArgbEvaluator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import org.w3c.dom.Text

private const val TAG = "MainActivity"
private const val INITIAL_TIP_PERCENT = 15
private const val EMPTY_AMOUNT = "₱0.00"
private const val EMPTY_SPLIT = "--"

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
    private lateinit var tvSplitAmount: TextView
    private lateinit var etCustomTip: EditText
    private lateinit var buttonCustomTip: Button
    private lateinit var tvCustomTipLabel: TextView

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
        tvSplitAmount = findViewById(R.id.tvSplitAmount)
        etCustomTip = findViewById(R.id.etCustomTip)
        buttonCustomTip = findViewById(R.id.buttomCustomTip)
        tvCustomTipLabel = findViewById(R.id.tvCustomTipLabel)

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
                computeTipAndTotal()

                if (switchSplit.isChecked) {
                    splitBill()
                }
            }
        })

        updateCurrency()
        switchUSD.setOnCheckedChangeListener(object: CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                updateCurrency()
            }
        })

        tvSplitAmount.text = EMPTY_SPLIT
        etNumberOfPeople.isFocusable = false
        switchSplit.setOnCheckedChangeListener(object: CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                if (p1) {
                    etNumberOfPeople.isFocusableInTouchMode = true
                    splitBill()
                } else {
                    etNumberOfPeople.isFocusable = false
                    tvSplitAmount.text = "--"
                    etNumberOfPeople.setText("")
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
                splitBill()
            }

        })

        buttonCustomTip.setOnClickListener(object: View.OnClickListener {
            override fun onClick(p0: View?) {
                if (tvCustomTipLabel.visibility != View.GONE) {
                    etCustomTip.visibility = View.GONE
                    tvCustomTipLabel.visibility = View.GONE
                    seekBarTip.isEnabled = true
                    // Revert tip UI to seekBar
                    tvTipPercent.text = "${seekBarTip.progress.toString()}%"
                    updateTipDescription(seekBarTip.progress)
                    computeTipAndTotal()
                } else {
                    etCustomTip.visibility = View.VISIBLE
                    tvCustomTipLabel.visibility = View.VISIBLE
                    seekBarTip.isEnabled = false
                    if (etCustomTip.text.isEmpty()) {
                        updateTipDescription(0)
                    } else {
                        updateTipDescription(etCustomTip.text.toString().toInt())
                    }

                    customTip()
                    computeTipAndTotal()
                }
            }

        })

        etCustomTip.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(p0: Editable?) {
                customTip()
                if (etCustomTip.text.isNotEmpty()) {
                    tvTipAmount.text = "x${etCustomTip.text.toString()}"
                }
                computeTipAndTotal()
            }
        })
    }

    private fun customTip() {
        if (etBaseAmount.text.isNotBlank()) {
            if (etCustomTip.text.isNotBlank()) {
                val customTip = etCustomTip.text.toString().toDouble()
                val baseAmount = etBaseAmount.text.toString().toDouble()
                val tipPercentage = ((customTip / baseAmount) * 100)

                Log.i(TAG, "customTip $customTip")
                Log.i(TAG, "baseAmount $baseAmount")
                Log.i(TAG, "tipPercentage $tipPercentage")

                if (tipPercentage != 0.00 && tipPercentage < 1) {
                    tvTipPercent.text = "<1%"
                } else {
                    tvTipPercent.text = "${tipPercentage.toInt()}%"
                }

                updateTipDescription(tipPercentage.toInt())

            } else {
                tvTipPercent.text = "0%"
            }
        }
    }

    private fun updateCurrency() {
        val currency = if (switchUSD.isChecked) "$" else "₱"
        tvTipAmount.text = tvTipAmount.text.toString().replaceFirstChar { currency }
        tvTotalAmount.text = tvTotalAmount.text.toString().replaceFirstChar { currency }

        if (switchSplit.isChecked) {
            tvSplitAmount.text = tvSplitAmount.text.toString().replaceFirstChar { currency }
        }

//        onSwitch()
    }

//    private fun onSwitch() {
//        val sharedPref = getPreferences(Context.MODE_PRIVATE)
//        val editor = sharedPref.edit()
//
//        switchUSD.isChecked = sharedPref.getBoolean("PREF_USD", false)
//        switchUSD.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                editor.putBoolean("PREF_USD", true)
//            } else {
//                editor.putBoolean("PREF_USD", false)
//            }
//        }
//
//        editor.apply()
//    }

    private fun splitBill() {
        if (switchSplit.isChecked) {
            if (etBaseAmount.text.isEmpty()) {
                tvSplitAmount.text = EMPTY_AMOUNT
                return
            }
            // If split enabled, divide tip and bill by number of people
            val numberOfPeople =
                if (etNumberOfPeople.text.isEmpty()) 2 else etNumberOfPeople.text.toString().toInt()

            val totalAmount = tvTotalAmount.text.toString().drop(1).toDouble()
            val splitAmount = totalAmount / numberOfPeople

            tvSplitAmount.text = "x%.2f".format(splitAmount)
            updateCurrency()
        }
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
            var color = ArgbEvaluator().evaluate(
                tipPercent.toFloat() / seekBarTip.max,
                ContextCompat.getColor(this, R.color.tip_worst),
                ContextCompat.getColor(this, R.color.tip_best)
            ) as Int

            Log.i(TAG, "$color")

            if (tipPercent.toFloat() / seekBarTip.max > 1) {
                color = ContextCompat.getColor(this, R.color.tip_best)
            }

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
//        val tipPercent = tvTipPercent.text.toString().dropLast(1).toInt()
        val tipPercent = seekBarTip.progress

        // Compute tip and total
        var tipAmount: Double

        if (tvCustomTipLabel.visibility == View.VISIBLE) {
            tipAmount = if (etCustomTip.text.isEmpty()) 0.00 else etCustomTip.text.toString().toDouble()
        } else {
            tipAmount = baseAmount * tipPercent / 100
        }

        val totalAmount = baseAmount + tipAmount

        // Update UI
        tvTipAmount.text = "x%.2f".format(tipAmount)
        tvTotalAmount.text = "x%.2f".format(totalAmount)

        updateCurrency()

        splitBill()
    }
}