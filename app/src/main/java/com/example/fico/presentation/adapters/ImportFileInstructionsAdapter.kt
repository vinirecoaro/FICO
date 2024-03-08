package com.example.fico.presentation.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fico.domain.model.ImportFileInstructionsComponents
import com.example.fico.presentation.fragments.expense.ImportFileInstructionsFragment

class ImportFileInstructionsAdapter(fa : FragmentActivity, private val contents : List<ImportFileInstructionsComponents>) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return contents.size
    }

    override fun createFragment(position: Int): Fragment {
        return ImportFileInstructionsFragment.newInstance(contents[position])
    }
}