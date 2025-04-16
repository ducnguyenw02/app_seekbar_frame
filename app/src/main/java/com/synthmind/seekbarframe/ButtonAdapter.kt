package com.synthmind.seekbarframe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.synthmind.seekbarframe.databinding.ItemButtonBinding

class ButtonAdapter(
    private val poses: List<String>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ButtonAdapter.ButtonViewHolder>() {

    inner class ButtonViewHolder(private val binding: ItemButtonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pose: String, position: Int) {
            binding.itemButton.text = pose
            binding.root.setOnClickListener {
                onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val binding = ItemButtonBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ButtonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        holder.bind(poses[position], position)
    }

    override fun getItemCount(): Int = poses.size
}