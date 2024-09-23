package th.ac.rmutto.shopdee

import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.ArrayList

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        //For an synchronous task
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val sharedPrefer = requireContext().getSharedPreferences(
            "appPrefer", Context.MODE_PRIVATE)
        val custID = sharedPrefer?.getString("custIDPref", null)
        val token = sharedPrefer?.getString("tokenPref", null)

        val barChartYearlySale: BarChart = root.findViewById(R.id.barChartYearlySale)
        val lineChartMonthlySale: LineChart = root.findViewById(R.id.lineChartMonthlySale)
        val pieChartTopFiveProduct: PieChart = root.findViewById(R.id.pieChartTopFiveProduct)

        showYearlySale(barChartYearlySale,custID!!, token!!)
        showMonthlySale(lineChartMonthlySale,custID, token)
        showTopFiveProduct(pieChartTopFiveProduct,custID, token)

        return root
    }

    private fun showYearlySale(chart: BarChart, custID: String, token: String) {
        val entries = ArrayList<BarEntry>()
        val labels = arrayListOf<String>()

        //Load data from API
        val url: String = getString(R.string.root_url) + getString(R.string.yearlySale_url) + custID

        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        val response = okHttpClient.newCall(request).execute()

        if (response.isSuccessful) {
            val res = JSONArray(response.body!!.string())
            if (res.length() > 0) {
                for (i in 0 until res.length()) {
                    val item: JSONObject = res.getJSONObject(i)
                    val index = java.lang.Float.valueOf(i.toString())
                    val value = java.lang.Float.valueOf(
                        item.getString("totalAmount"))
                    entries.add(BarEntry(index, value))
                    labels.add(item.getString("year"))
                }
            }
        }

        val dataset = BarDataSet(entries, "")
        dataset.valueTextSize = 12f
        dataset.setColors(*ColorTemplate.COLORFUL_COLORS) // Set the colors
        dataset.valueFormatter = MyValueFormatter("###,###,###,##0.0", "")
        val data = BarData(dataset)
        chart.data = data

        // Disable the chart description label
        chart.description.isEnabled = false
        //chart.description.text = "ยอดการสั่งซื้อรายเดือน (บาท)"

        // Hide X axis grid lines
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        val xAxis = chart.xAxis
        xAxis.labelCount = labels.count()
        xAxis.textSize = 12f
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE

        // Hide left Y axis grid lines
        val leftAxis = chart.axisLeft
        leftAxis.textSize = 12f
        leftAxis.setDrawGridLines(false) // Hides grid lines on the left Y axis

        // Hide right Y axis
        val rightAxis = chart.axisRight
        rightAxis.textSize = 12f
        rightAxis.isEnabled = false // Disable the right Y axis

        // Hide background grid lines and borders
        chart.setDrawGridBackground(false) // Disable grid background if any
        chart.setDrawBorders(false)        // Remove chart border if enabled

        // Disable the legend
        val legend = chart.legend
        legend.isEnabled = false

        // Refresh the chart
        chart.invalidate()

    }

    private fun showMonthlySale(chart: LineChart, custID: String, token: String) {
        val entries = ArrayList<Entry>() // Use Entry for LineChart instead of BarEntry
        val labels = arrayListOf<String>()
        val months = arrayOf(
            "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
            "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."
        )

        // Load data from API
        val url: String = getString(R.string.root_url) + getString(R.string.monthlySale_url) + custID

        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        val response = okHttpClient.newCall(request).execute()

        if (response.isSuccessful) {
            val res = JSONArray(response.body!!.string())
            if (res.length() > 0) {
                for (i in 0 until res.length()) {
                    val item: JSONObject = res.getJSONObject(i)
                    val index = i.toFloat() // Use `i.toFloat()` for LineChart index
                    val value = item.getString("totalAmount").toFloat()
                    entries.add(Entry(index, value)) // Add Entry for LineChart
                    labels.add(months[item.getString("month").toInt() - 1])
                }
            }
        }

        // Create LineDataSet instead of BarDataSet
        val dataset = LineDataSet(entries, "")
        dataset.valueTextSize = 12f
        dataset.setColors(*ColorTemplate.COLORFUL_COLORS) // Set the colors for lines
        dataset.valueFormatter = MyValueFormatter("###,###,###,##0.0", "")
        dataset.setDrawCircles(true) // Draw circles at data points
        dataset.setCircleColor(R.color.black) // Set circle color
        dataset.setLineWidth(2f) // Set line width
        dataset.setDrawFilled(true) // Enable filled line

        val data = LineData(dataset)
        chart.data = data

        // Disable the chart description label
        chart.description.isEnabled = false

        // Hide X axis grid lines
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        val xAxis = chart.xAxis
        xAxis.labelCount = labels.count()
        xAxis.textSize = 12f
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE

        // Hide left Y axis grid lines
        val leftAxis = chart.axisLeft
        leftAxis.textSize = 12f
        leftAxis.setDrawGridLines(false) // Hides grid lines on the left Y axis

        // Hide right Y axis
        val rightAxis = chart.axisRight
        rightAxis.textSize = 12f
        rightAxis.isEnabled = false // Disable the right Y axis

        // Hide background grid lines and borders
        chart.setDrawGridBackground(false) // Disable grid background if any
        chart.setDrawBorders(false)        // Remove chart border if enabled

        // Disable the legend
        val legend = chart.legend
        legend.isEnabled = false

        // Refresh the chart
        chart.invalidate()
    }

    private fun showTopFiveProduct(chart: PieChart, custID: String, token: String) {
        val entries = ArrayList<PieEntry>()

        // Load data from API
        val url: String = getString(R.string.root_url) + getString(R.string.topFiveProduct_url) + custID

        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        val response = okHttpClient.newCall(request).execute()

        if (response.isSuccessful) {
            val res = JSONArray(response.body!!.string())
            if (res.length() > 0) {
                for (i in 0 until res.length()) {
                    val item: JSONObject = res.getJSONObject(i)
                    val value = item.getString("totalAmount").toFloat()
                    val label = item.getString("productName")
                    entries.add(PieEntry(value, label))
                }
            }
        }

        // Setup PieDataSet
        val dataset = PieDataSet(entries, "")
        dataset.valueTextSize = 12f
        dataset.setColors(*ColorTemplate.COLORFUL_COLORS) // Set color template for chart slices
        dataset.valueFormatter = MyValueFormatter("###,###,###,##0.0", "") // Custom value formatter

        // Create PieData
        val data = PieData(dataset)
        chart.data = data

        // Chart formatting options
        chart.description.isEnabled = false // Hide the description label
        chart.isDrawHoleEnabled = true // Show hole in the center
        chart.setUsePercentValues(true) // Display values as percentages
        chart.setEntryLabelTextSize(12f) // Set text size for labels inside pie chart
        chart.setEntryLabelColor(R.color.black) // Set the color for entry labels

        // Define legend
        val legend = chart.legend
        legend.isEnabled = true
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        chart.invalidate() // Refresh the chart
    }

}


class MyValueFormatter(pattern: String?, suffix: String) :
    ValueFormatter() {
    private val mFormat: DecimalFormat
    private val suffix: String
    override fun getFormattedValue(value: Float): String {
        return mFormat.format(value.toDouble()) + suffix
    }

    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        return if (axis is XAxis) {
            mFormat.format(value.toDouble())
        } else if (value > 0) {
            mFormat.format(value.toDouble()) + suffix
        } else {
            mFormat.format(value.toDouble())
        }
    }

    init {
        mFormat = DecimalFormat(pattern)
        this.suffix = suffix
    }
}