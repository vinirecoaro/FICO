package com.example.fico.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fico.model.ImportFileInstructionsComponents
import com.example.fico.ui.fragments.expense.ImportFileInstructionsFragment

class ImportFileInstructionsAdapter(fa : FragmentActivity, private val contents : List<ImportFileInstructionsComponents>) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return contents.size
    }

    override fun createFragment(position: Int): Fragment {
        return ImportFileInstructionsFragment.newInstance(contents[position])
    }
}