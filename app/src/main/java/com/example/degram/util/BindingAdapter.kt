package com.example.degram.util

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.degram.R
import com.example.degram.ui.insights.Metrics

@BindingAdapter("visibility")
fun setVisibility(view: View, condition: Boolean) {
    view.isVisible = condition
}

@BindingAdapter("emoji")
fun setMetricEmoji(textView: TextView, metrics: LiveData<Metrics<Long>>?) {
    val context = textView.context
    when (val data = metrics?.value) {
        is Metrics.DailyAverage -> {
            textView.text = if (data.currentUsage < data.data) context.getString(R.string.lower_than_daily_average_emoji) else context.getString(R.string.greater_than_daily_average_emoji)
        }
        is Metrics.GlobalAverage -> {
            textView.text = if (data.data <= Constants.GLOBAL_USAGE_AVERAGE) context.getString(R.string.lower_than_global_average_emoji) else context.getString(R.string.greater_than_global_average_emoji)
        }
        is Metrics.StreakCount -> {
            textView.text = if (data.data == 0L) context.getString(R.string.no_streaks_emoji) else context.getString(R.string.streaks_emoji)
        }
    }
}

@BindingAdapter("text")
fun setMetricsText(textView: TextView, metrics: LiveData<Metrics<Long>>?) {
    val context = textView.context
    when (val data = metrics?.value) {
        is Metrics.DailyAverage -> {
            textView.text = if (data.currentUsage < data.data) context.getString(R.string.lower_than_daily_average) else context.getString(R.string.greater_than_daily_average)
        }
        is Metrics.GlobalAverage -> {
            textView.text = if (data.data <= Constants.GLOBAL_USAGE_AVERAGE) context.getString(R.string.lower_than_global_average) else context.getString(R.string.greater_than_global_average)
        }
        is Metrics.StreakCount -> {
            textView.text = if (data.data > 0) context.resources.getQuantityString(R.plurals.streaks_text, data.data.toInt(), data.data.toInt()) else context.getString(R.string.no_streaks_text)
        }
    }
}

@BindingAdapter("infoText")
fun setInfoText(textView: TextView, metrics: Metrics<Long>?) {
    val context = textView.context
    when (metrics) {
        is Metrics.DailyAverage -> {
            textView.text = context.getString(R.string.daily_average_info, formatTime(metrics.currentUsage), formatTime(metrics.data))
        }
        is Metrics.GlobalAverage -> {
            textView.text = context.getString(R.string.global_average_info, formatTime(metrics.data))
        }
        is Metrics.StreakCount -> {
            textView.text = context.getString(R.string.streaks_info)
        }
        is Metrics.ScoreInfo -> textView.text = context.getString(R.string.score_info)
    }
}

@BindingAdapter("scoreDescription")
fun setScoreDescription(textView: TextView, score: LiveData<Int>?) {
    textView.text = if (score?.value == 100) textView.context.getString(R.string.perfect_degram_score_description) else textView.context.getString(R.string.degram_score_description)
}

@BindingAdapter("compliment")
fun setCompliment(textView: TextView, score: LiveData<Int>?) {
    val data = score?.value
    val context = textView.context
    if (data != null) {
        textView.text = when (data) {
            100 -> context.getString(R.string.degram_score_100)
            in 96..99 -> context.getString(R.string.degram_score_96)
            in 92..95 -> context.getString(R.string.degram_score_92)
            in 88..91 -> context.getString(R.string.degram_score_88)
            in 84..87 -> context.getString(R.string.degram_score_84)
            in 80..83 -> context.getString(R.string.degram_score_80)
            in 76..79 -> context.getString(R.string.degram_score_76)
            in 72..75 -> context.getString(R.string.degram_score_72)
            in 68..71 -> context.getString(R.string.degram_score_68)
            in 64..67 -> context.getString(R.string.degram_score_64)
            in 60..63 -> context.getString(R.string.degram_score_60)
            in 30..59 -> context.getString(R.string.degram_score_30)
            else -> context.getString(R.string.degram_score_0)
        }
    }
}

@BindingAdapter("setProfilePhoto")
fun setProfilePhoto(imageView: ImageView, imageUrl: String?) {
    if (imageUrl != null) {
        Glide.with(imageView.context)
                .load(imageUrl)
                .apply(RequestOptions().placeholder(R.drawable.default_profile_picture))
                .circleCrop()
                .into(imageView)
    } else {
        imageView.isVisible = false
    }
}

@BindingAdapter("rank")
fun rank(textView: TextView, rank: Int) {
   textView.text = when (rank) {
       1 -> textView.context.getString(R.string.rank_1)
       2 -> textView.context.getString(R.string.rank_2)
       3 -> textView.context.getString(R.string.rank_3)
       else -> "#${rank}"
   }
}