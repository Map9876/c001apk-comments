package com.example.c001apk.ui.feed

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.viewModels
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.base.BaseViewActivity
import com.example.c001apk.ui.feed.question.FeedQuestionFragment
import com.example.c001apk.ui.feed.vote.FeedVoteFragment
import com.example.c001apk.ui.others.WebViewActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback

@AndroidEntryPoint
class FeedActivity : BaseViewActivity<FeedViewModel>() {

    override val viewModel by viewModels<FeedViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<FeedViewModel.Factory> { factory ->
                factory.create(
                    intent.getStringExtra("id").orEmpty(),
                    intent.getStringExtra("rid"),
                    intent.getBooleanExtra("viewReply", false),
                )
            }
        }
    )

    override fun getSavedData(savedInstanceState: Bundle?) {
        if (viewModel.feedData == null) {
            viewModel.feedData = if (SDK_INT >= 33)
                savedInstanceState?.getParcelable("feedData", HomeFeedResponse.Data::class.java)
            else savedInstanceState?.getParcelable("feedData")
        }
    }

    // 酷安对 /v6/feed/detail 启用了验证码（err_request_captcha_v2），
    // 详情接口必败；首屏加载失败时提供网页版兜底查看。
    private var webFallbackShown = false

    override fun initData() {
        viewModel.activityState.observe(this) { state ->
            if ((state is LoadingState.LoadingError || state is LoadingState.LoadingFailed)
                && viewModel.feedData == null && !webFallbackShown
            ) {
                webFallbackShown = true
                showWebFallbackDialog()
            }
        }
        if (viewModel.isAInit) {
            viewModel.isAInit = false
            if (viewModel.feedData == null) {
                viewModel.activityState.value = LoadingState.Loading
            } else {
                viewModel.handleFeedData()
            }
        }
    }

    private fun showWebFallbackDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("详情加载失败")
            .setMessage("酷安已对动态详情接口启用验证码，可改用网页版查看该动态")
            .setPositiveButton("网页版打开") { _, _ ->
                startActivity(
                    Intent(this, WebViewActivity::class.java).apply {
                        putExtra("url", "https://www.coolapk.com/feed/${viewModel.id}")
                    }
                )
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun fetchData() {
        viewModel.fetchFeedData()
    }

    @SuppressLint("CommitTransaction")
    override fun beginTransaction() {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    when (viewModel.feedType) {
                        "vote" -> FeedVoteFragment()
                        "question" -> FeedQuestionFragment()
                        else -> FeedFragment()
                    }
                )
                .commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.feedData?.let {
            outState.putParcelable("feedData", it)
        }
        super.onSaveInstanceState(outState)
    }

}