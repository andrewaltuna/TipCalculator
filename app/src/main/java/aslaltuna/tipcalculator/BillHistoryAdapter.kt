package aslaltuna.tipcalculator

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BillHistoryAdapter( val context: Context, val billHistory: List<BillHistoryItem>, val recyclerViewInterface: RecyclerViewInterface) : RecyclerView.Adapter<BillHistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface RecyclerViewInterface {
        fun onItemLongClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_bill_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val billItem = billHistory[position]
        val tvItemTitle = holder.itemView.findViewById<TextView>(R.id.tvItemTitle)
        val tvItemTipAmount = holder.itemView.findViewById<TextView>(R.id.tvItemTipAmount)
        val tvItemTotalAmount = holder.itemView.findViewById<TextView>(R.id.tvItemTotalAmount)
        val tvDateCreated = holder.itemView.findViewById<TextView>(R.id.tvDateCreated)

        val numSplit = if (billItem.numSplit == 0) "" else "/${billItem.numSplit} ≈ ${billItem.splitAmount}/person"

        tvItemTitle.text = billItem.title
        tvItemTipAmount.text = billItem.tipAmount
        tvItemTotalAmount.text = billItem.totalAmount + numSplit
        tvDateCreated.text = billItem.dateCreated

        holder.itemView.setOnLongClickListener {
            recyclerViewInterface.onItemLongClick(position)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount() = billHistory.size

}
