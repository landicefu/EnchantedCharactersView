package tw.lifehackers.sample.enchantedcharacters

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.BounceInterpolator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private val MONTHS = listOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        )
    }

    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enchantedCharactersView2.interpolator = BounceInterpolator()
        buttonNext.setOnClickListener {
            enchantedCharactersView.text = MONTHS[++index % MONTHS.size]
            enchantedCharactersView2.text = MONTHS[index % MONTHS.size]
            enchantedCharactersView3.text = MONTHS[index % MONTHS.size]
        }
    }
}
