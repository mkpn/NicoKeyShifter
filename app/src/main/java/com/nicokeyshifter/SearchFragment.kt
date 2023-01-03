package com.nicokeyshifter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nicokeyshifter.R
import com.nicokeyshifter.databinding.SearchBinding

class SearchFragment : Fragment() {
    private lateinit var binding: SearchBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.search, null, false)
        binding.executeSearch.setOnClickListener {
            sendSearchAction()
        }
        binding.searchText.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEARCH && textView.text.isNotEmpty()) {
                sendSearchAction()
                return@setOnEditorActionListener true
            }
            false
        }
        return binding.root
    }

    private fun sendSearchAction() {
        findNavController().navigate(R.id.actionSearch)
    }
}