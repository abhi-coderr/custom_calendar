package com.example.mycustomcalendar

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mycustomcalendar.databinding.ActivityMainBinding
import com.example.mycustomcalendar.databinding.CalendarCustomLayoutBinding
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setUpBinding()
    }

    private fun setUpBinding() = binding.apply {

        var currentMonth = YearMonth.now()
        val currentYear = currentMonth.year
        val currentMonthForText = currentMonth.month
        val startMonth = currentMonth.minusMonths(100)  // Adjust as needed
        val endMonth = currentMonth.plusMonths(100)  // Adjust as needed
        val daysOfWeek = firstDayOfWeekFromLocale()
        calendarView.setup(startMonth, endMonth, daysOfWeek)

        calendarView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return rv.scrollState == RecyclerView.SCROLL_STATE_DRAGGING
            }
        })

        var selectedDate: LocalDate? = null

        calendarYearText.text = currentYear.toString()
        calendarMonthText.text = currentMonthForText.toString()

        nextMonthImg.setOnClickListener {
            val yearMonthToScrollRight: YearMonth = currentMonth.nextMonth
            currentMonth = yearMonthToScrollRight
            val scrollYear = currentMonth.year.toString()
            calendarView.scrollToMonth(currentMonth)
            calendarMonthText.text = currentMonth.month.toString()
            calendarYearText.text = scrollYear
        }

        previousMonthImg.setOnClickListener {
            val yearMonthToScrollLeft: YearMonth = currentMonth.previousMonth
            currentMonth = yearMonthToScrollLeft
            calendarView.scrollToMonth(yearMonthToScrollLeft)
            val scrollYear = currentMonth.year.toString()
            calendarMonthText.text = currentMonth.month.toString()
            calendarYearText.text = scrollYear
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            // With ViewBinding
            val textView = CalendarCustomLayoutBinding.bind(view).calendarDayText
            lateinit var day: CalendarDay

            init {
                textView.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        // Keep a reference to any previous selection
                        // in case we overwrite it and need to reload it.
                        val currentSelection = selectedDate
                        if (currentSelection == day.date) {
                            // If the user clicks the same date, clear selection.
                            selectedDate = null
                            // Reload this date so the dayBinder is called
                            // and we can REMOVE the selection background.
                            calendarView.notifyDateChanged(currentSelection)
                        } else {
                            selectedDate = day.date
                            // Reload the newly selected date so the dayBinder is
                            // called and we can ADD the selection background.
                            calendarView.notifyDateChanged(day.date)
                            currentSelection != null?.let { it1 ->
                                it1 {
                                    // We need to also reload the previously selected
                                    // date so we can REMOVE the selection background.
                                    if (currentSelection != null) {
                                        calendarView.notifyDateChanged(currentSelection)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.textView
                textView.text = data.date.dayOfMonth.toString()
                if (data.position == DayPosition.MonthDate) {
                    // Show the month dates. Remember that views are reused!
                    textView.visibility = View.VISIBLE
                    if (data.date == selectedDate) {
                        // If this is the selected date, show a round background and change the text color.
                        textView.setTextColor(Color.WHITE)
                        textView.setBackgroundResource(R.drawable.selection_background)
                    } else {
                        // If this is NOT the selected date, remove the background and reset the text color.
                        textView.setTextColor(Color.BLACK)
                        textView.background = null
                    }
                } else {
                    // Hide in and out dates
                    textView.visibility = View.INVISIBLE
                }
            }
        }
    }

}