package com.nicokeyshifter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.nicokeyshifter.R
import com.nicokeyshifter.databinding.SearchBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private lateinit var binding: SearchBinding

    @Inject
    lateinit var apiService: ApiService
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.search, null, false)
        binding.lifecycleOwner = viewLifecycleOwner
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    val response = apiService.search("初音ミク")
                    println("デバッグ response is ${response.body().toString()}")
                    if (response.isSuccessful) {
                        val adapter = VideoAdapter(response.body()!!.data)
                        binding.recyclerView.adapter = adapter
                    }
                }
            }
        }

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