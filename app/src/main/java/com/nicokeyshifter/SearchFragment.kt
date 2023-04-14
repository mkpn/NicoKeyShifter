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
import com.nicokeyshifter.databinding.SearchBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
        binding.executeSearch.setOnClickListener {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    launch {
                        val response = apiService.search(binding.searchText.text.toString())
                        if (response.isSuccessful) {
                            val adapter = VideoAdapter(response.body()!!.data) {
                                sendSearchAction(it)
                            }
                            binding.recyclerView.adapter = adapter
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun sendSearchAction(videoId: String) {
        val action = SearchFragmentDirections.actionSearch(videoId)
        findNavController().navigate(action)
    }
}