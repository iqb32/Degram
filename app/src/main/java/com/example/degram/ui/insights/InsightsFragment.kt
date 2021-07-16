package com.example.degram.ui.insights

import android.app.usage.UsageStats
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.degram.R
import com.example.degram.databinding.FragmentInsightsBinding
import com.example.degram.databinding.InfoDialogBinding
import com.example.degram.util.YAxisFormatter
import com.example.degram.util.asGraphData
import com.example.degram.util.getXAxisData
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InsightsFragment : Fragment() {

    private val viewModel: InsightsViewModel by viewModels()
    private lateinit var binding: FragmentInsightsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_insights, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.info.observe(viewLifecycleOwner, { setUpInfoDialog(it) })

        binding.degramScoreLayout.learnMore.setOnClickListener { setUpInfoDialog(Metrics.ScoreInfo) }

        viewModel.graphData.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) showChart(it)
        })


        binding.missingPermissions.button.setOnClickListener { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }

        binding.noData.button.setOnClickListener {
            viewModel.getStats()
        }

        return binding.root
    }


    private fun showChart(data: List<UsageStats>) {
        val chartData = BarData(data.asGraphData(requireContext()))
        chartData.barWidth = 0.4F
        binding.chart.apply {
            description.isEnabled = false
            xAxis.setDrawGridLines(false)
            xAxis.typeface = ResourcesCompat.getFont(context, R.font.regular)
            axisLeft.setDrawGridLines(false)
            axisRight.setDrawGridLines(false)
            axisRight.isEnabled = false
            legend.isEnabled = false
            axisRight.setDrawLabels(false)
            axisLeft.setDrawLabels(true)
            axisLeft.typeface = ResourcesCompat.getFont(context, R.font.regular)
            isDoubleTapToZoomEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = IndexAxisValueFormatter(data.getXAxisData())
            axisLeft.valueFormatter = YAxisFormatter()
            axisLeft.axisMinimum = 0F
            isScaleXEnabled = false
            isScaleYEnabled = false
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            setData(chartData)
            animateY(1500)
            invalidate()
        }
        binding.chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e != null) {
                    viewModel.setUsage(data[e.x.toInt() - 1])
                }
            }

            override fun onNothingSelected() {
                viewModel.setUsage(data.last())
            }
        })
    }


    private fun setUpInfoDialog(data: Metrics<Long>) {
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.bottomSheetDialog)
        val bindingSheet = DataBindingUtil.inflate<InfoDialogBinding>(
                layoutInflater,
                R.layout.info_dialog,
                null,
                false
        )
        bindingSheet.data = data
        bottomSheet.setContentView(bindingSheet.root)
        bottomSheet.show()
    }


}