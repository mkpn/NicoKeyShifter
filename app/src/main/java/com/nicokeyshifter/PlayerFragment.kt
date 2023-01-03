package com.nicokeyshifter

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.nicokeyshifter.R
import com.nicokeyshifter.databinding.PlayerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.pow

@AndroidEntryPoint
class PlayerFragment : Fragment() {

    private lateinit var binding: PlayerBinding
    private val viewModel: PlayerViewModel by viewModels()

    private var currentMediaItemIndex = 0
    private var playbackPosition = 0L
    private var playWhenReady = true
    private var mediaPlayer: ExoPlayer? = null
    private var currentKey: Double = 0.0
    private var dataSourceUri: Uri? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.player, null, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel

        initWebViewSettings()

        initWebViewClient()

        loadVideoInWebView()

        lifecycleScope.launch(Dispatchers.IO) {
            ApiRequestService().getMusicRanking()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentKeyValue.collect {
                    updatePitch(it.toDouble())
                }
            }
        }

        binding.pitchUp.setOnClickListener {
            pitchUp()
        }

        binding.pitchDown.setOnClickListener {
            pitchDown()
        }

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        return binding.root
    }

    private fun loadVideoInWebView() {
        val html =
            "<html><body><script type=\"application/javascript\" src=\"https://embed.nicovideo.jp/watch/sm41403930/script?w=320&h=180\"></script></body></html>"
        val encodedHtml = Base64.encodeToString(html.toByteArray(), Base64.NO_PADDING)
        binding.webView.loadData(encodedHtml, "text/html", "base64")
    }

    private fun initWebViewSettings() {
        binding.webView.settings.run {
            javaScriptEnabled = true
            useWideViewPort = false
            mediaPlaybackRequiresUserGesture = false //動画再生に必要
            domStorageEnabled = true //動画再生に必要
        }
    }

    private fun initWebViewClient() {
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                return super.shouldInterceptRequest(view, request)
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                //master.m3u8が含まれるURLにアクセスすればいける
                url?.let {
                    if (url.contains("master.m3u8")) {
                        dataSourceUri = Uri.parse(it)
                        initPlayer(dataSourceUri!!)
                        binding.webView.run {
                            // この辺でwebview消すとニコニコ側がhlsのソース無効化するらしく読み込み完了しないので注意
                            webViewClient = object : WebViewClient() {}
                            webChromeClient = null
                        }
                        binding.playerOverLay.visibility = View.GONE
                    }
                }
                super.onLoadResource(view, url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                println("デバッグ on page finished")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mediaPlayer == null && dataSourceUri != null) {
            initPlayer(dataSourceUri!!)
        }
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun pitchUp() {
        viewModel.pitchUp()
    }

    private fun pitchDown() {
        viewModel.pitchDown()
    }

    private fun updatePitch(key: Double) {
        val playbackParams = PlaybackParameters(
            1.0f,
            generatePitchFrequency(key)
        )
        mediaPlayer?.playbackParameters = playbackParams
    }

    private fun generatePitchFrequency(key: Double): Float {
        return 2.0.pow(key / 12).toFloat()
    }

    private fun releasePlayer() {
        mediaPlayer?.let {
            playWhenReady = it.playWhenReady
            playbackPosition = it.currentPosition
            currentMediaItemIndex = it.currentMediaItemIndex
            it.release()
            mediaPlayer = null
        }
    }

    private fun initPlayer(url: Uri) {
        val mediaSource = buildMediaSource(url)

        mediaPlayer = ExoPlayer.Builder(requireContext()).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    when (playbackState) {
                        ExoPlayer.STATE_IDLE -> "STATE_IDLE"
                        ExoPlayer.STATE_BUFFERING -> "STATE_BUFFERING"
                        ExoPlayer.STATE_READY -> "STATE_READY"
                        ExoPlayer.STATE_ENDED -> {
                            // 繰り返し再生させちゃう
                            seekTo(0)
                        }
                        else -> "UNKNOWN"
                    }

                }
            })
            setMediaSource(mediaSource)
            prepare()
            updatePitch(currentKey)
        }

        binding.playerView.player = mediaPlayer
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSource.Factory(requireContext())
        val mediaItem = MediaItem.fromUri(uri)
        return HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
    }

}