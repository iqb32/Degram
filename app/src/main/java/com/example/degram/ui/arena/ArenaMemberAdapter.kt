package com.example.degram.ui.arena

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.degram.data.Member
import com.example.degram.databinding.ItemArenaMemberBinding


class ArenaMemberAdapter : ListAdapter<Member, ArenaMemberAdapter.MembersViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersViewHolder {
        return MembersViewHolder(ItemArenaMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MembersViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    inner class MembersViewHolder(private val binding: ItemArenaMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Member) {
            binding.member = item
            binding.rank = currentList.indexOf(item)
            binding.executePendingBindings()
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Member>() {
        override fun areItemsTheSame(oldItem: Member, newItem: Member): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Member, newItem: Member): Boolean {
            return oldItem == newItem
        }

    }
}