package com.example.mythical.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.Paint
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.mythical.data.Product
import com.example.mythical.databinding.BestDealsRvItemBinding
import com.example.mythical.databinding.ProductRvItemBinding
import com.example.mythical.helper.getProductPrice


class BestProductAdapter: RecyclerView.Adapter<BestProductAdapter.BestProductsViewHolder>() {

    inner class BestProductsViewHolder(private val binding: ProductRvItemBinding): ViewHolder(binding.root){
        fun bind(product: Product){
            binding.apply {
                val priceAfterOffer = product.offerPercentage.getProductPrice(product.price)
                tvNewPrice.text = "$ ${String.format("%.2f",priceAfterOffer)}"
                tvPrice.paintFlags = android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                if(product.offerPercentage == null)
                    tvNewPrice.visibility = View.INVISIBLE
                tvPrice.text = "$ ${product.price}"
                tvName.text = product.name
            }
        }
    }

    private val diffCalBack = object : DiffUtil.ItemCallback<Product>(){
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,diffCalBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestProductsViewHolder {
        return BestProductsViewHolder(
            ProductRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: BestProductsViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((Product) -> Unit)? = null
}