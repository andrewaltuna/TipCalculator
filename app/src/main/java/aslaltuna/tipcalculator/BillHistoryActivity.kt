package aslaltuna.tipcalculator

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_bill_history.*
import java.io.*

class BillHistoryActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BillHistoryActivity"
        private const val FILENAME = "BillHistory.data"
    }

    private lateinit var billHistoryAdapter: BillHistoryAdapter
    private lateinit var billHistory: MutableList<BillHistoryItem>
    private lateinit var snackBar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_history)

        billHistory = deserializeBillHistory(this).toMutableList()

        val billHistoryItem = intent.getSerializableExtra("BILL_HISTORY_ITEM")

        if (billHistoryItem != null) {
            val billItem = billHistoryItem as BillHistoryItem
            billHistory.add(billItem)
            serializeBillHistory(this@BillHistoryActivity, billHistory)
        }

        rvBillHistory.layoutManager = LinearLayoutManager(this)
        billHistoryAdapter = BillHistoryAdapter(this, billHistory, object: BillHistoryAdapter.RecyclerViewInterface {
            override fun onItemLongClick(position: Int) {
                // DELETE BILL ITEM
                billHistory.remove(billHistory[position])
                billHistoryAdapter.notifyItemRemoved(position)
                serializeBillHistory(this@BillHistoryActivity, billHistory)
                emptyMessage()
            }

        })
        rvBillHistory.adapter = billHistoryAdapter

        emptyMessage()

        Snackbar.make(findViewById(android.R.id.content), "Long press to delete an entry.", Snackbar.LENGTH_INDEFINITE)
            .setAction("DISMISS", View.OnClickListener {  })
            .show()
    }

    private fun emptyMessage() {
        tvEmpty.visibility = if (billHistory.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun serializeBillHistory(context: Context, billHistory: List<BillHistoryItem>) {
        ObjectOutputStream(FileOutputStream(getDataFile(context))).use { it.writeObject(billHistory) }
    }

    private fun deserializeBillHistory(context: Context) : List<BillHistoryItem> {
        val dataFile = getDataFile(context)

        if (!dataFile.exists()) {
            Log.i(TAG, "File initialization!")
            return emptyList()
        }

        ObjectInputStream(FileInputStream(dataFile)).use { return it.readObject() as List<BillHistoryItem> }
    }

    private fun getDataFile(context: Context): File {
        Log.i(TAG, "Getting file from directory ${context.filesDir}")
        return File(context.filesDir, FILENAME)
    }

}