package aslaltuna.tipcalculator

import android.animation.ArgbEvaluator
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_bill_history.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

data class BillHistoryItem(val title: String, val tipAmount: String, val totalAmount: String, val numSplit: Int, val splitAmount: String,  val dateCreated: String) : Serializable

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val INITIAL_TIP_PERCENT = 15
        private const val EMPTY_AMOUNT = "₱0.00"
        private const val EMPTY_SPLIT = "--"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekBarTip.progress = INITIAL_TIP_PERCENT
        tvTipPercent.text = "$INITIAL_TIP_PERCENT%"
        updateTipDescription(INITIAL_TIP_PERCENT)
        seekBarTip.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, percentage: Int, p2: Boolean) {
                Log.i(TAG, "onProgressChanged $percentage")
                tvTipPercent.text = "$percentage%"
                computeTipAndTotal()
                updateTipDescription(percentage)
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
                if (etBaseAmount.text.toString() == ".") {
                    etBaseAmount.setText("0.")
                    etBaseAmount.setSelection(etBaseAmount.length())
                }

                if (tvCustomTipLabel.visibility == View.VISIBLE) customTip()

                computeTipAndTotal()

                if (switchSplit.isChecked) splitBill()
            }
        })

        updateCurrency()
        switchUSD.setOnCheckedChangeListener { _, isChecked -> updateCurrency() }

        tvSplitAmount.text = EMPTY_SPLIT
        etNumberOfPeople.isFocusable = false
        switchSplit.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                etNumberOfPeople.isFocusableInTouchMode = true
                splitBill()
            } else {
                etNumberOfPeople.isFocusable = false
                tvSplitAmount.text = "--"
                etNumberOfPeople.text.clear()
            }
        }

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
                    tvTipPercent.text = "${seekBarTip.progress}%"
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
                if (etCustomTip.text.toString() == ".") {
                    etCustomTip.setText("0.")
                    etCustomTip.setSelection(etCustomTip.length())
                }

                if (etCustomTip.text.isNotEmpty()) {
                    tvTipAmount.text = "x${etCustomTip.text}"
                }
                customTip()
                computeTipAndTotal()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.miSave) {
            if (etBaseAmount.text.isEmpty()) {
                Toast.makeText(this, "Price must be filled.", Toast.LENGTH_LONG).show()
                return true
            }

            showAlertDialogue()
        }

        if (item.itemId == R.id.miHistory) {
            val intent = Intent(this@MainActivity, BillHistoryActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialogue() {

        val billFormView = LayoutInflater.from(this).inflate(R.layout.dialog_create_bill_item, null)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Title")
            .setView(billFormView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Ok", null)
            .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val title = billFormView.findViewById<EditText>(R.id.etBillItemTitle).text.toString()

            if (title.trim().isEmpty()){
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            var numPeople = 0
            var splitAmount = "--"

            if (switchSplit.isChecked) {
                numPeople = if (etNumberOfPeople.text.isEmpty()) 2 else etNumberOfPeople.text.toString().toInt()
                splitAmount = tvSplitAmount.text.toString()
            }

            val billItem = BillHistoryItem(
                title,
                tvTipAmount.text.toString(),
                tvTotalAmount.text.toString(),
                numPeople,
                splitAmount,
                getCurrentDate()
            )

            Log.i(TAG, "Saved!")

            val intent = Intent(this@MainActivity, BillHistoryActivity::class.java)
            intent.putExtra("BILL_HISTORY_ITEM", billItem)
            startActivity(intent)

            dialog.dismiss()
        }
    }

    private fun getCurrentDate(): String {
        val rawDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat.getDateInstance() //or use getDateInstance()

        return dateFormat.format(rawDate)
    }

    private fun customTip() {
        if (etBaseAmount.text.isNotBlank()) {
            if (etCustomTip.text.isNotBlank()) {
                val customTip = etCustomTip.text.toString().toDouble()
                val baseAmount = etBaseAmount.text.toString().toDouble()
                val tipPercentage = ((customTip / baseAmount) * 100)

                tvTipPercent.text = if (tipPercentage != 0.00 && tipPercentage < 1) "<1%"
                                    else if (tipPercentage > 100) ">100%"
                                    else "${tipPercentage.toInt()}%"

                updateTipDescription(tipPercentage.toInt())
                seekBarTip.progress = (customTip / baseAmount * 100).toInt()

            }
            else {
                seekBarTip.progress = 0
            }
        } else {
            seekBarTip.progress = 0
        }
    }

    private fun updateCurrency() {
        val currency = if (switchUSD.isChecked) "$" else "₱"
        tvTipAmount.text = tvTipAmount.text.toString().replaceFirstChar { currency }
        tvTotalAmount.text = tvTotalAmount.text.toString().replaceFirstChar { currency }

        if (switchSplit.isChecked) {
            tvSplitAmount.text = tvSplitAmount.text.toString().replaceFirstChar { currency }
        }
    }

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

            tvSplitAmount.text = "x" + "%.2f".format(splitAmount)
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
        val tipPercent = seekBarTip.progress

        // Compute tip and total

        var tipAmount: Double = if (tvCustomTipLabel.visibility == View.VISIBLE) {
            if (etCustomTip.text.isEmpty()) 0.00 else etCustomTip.text.toString().toDouble()
        } else {
            baseAmount * tipPercent / 100
        }

        val totalAmount = baseAmount + tipAmount

        // Update UI
        tvTipAmount.text = "x" + "%.2f".format(tipAmount)
        tvTotalAmount.text = "x" + "%.2f".format(totalAmount)

        updateCurrency()

        splitBill()
    }
}