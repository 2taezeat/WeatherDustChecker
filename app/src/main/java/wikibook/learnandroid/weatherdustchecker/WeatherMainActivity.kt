package wikibook.learnandroid.weatherdustchecker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class WeatherMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_main_activity)

        //
        supportActionBar?.hide() // 상단 액션바를 이용하지 않을 예정이므로, 숨김

//        val fragment = WeatherPageFragment.newInstance(37.5,126.9)
//
//        // 프래그먼트 매너저의 add 메서드를 호출해서 생성한 날씨 프래그먼트를 추가합니다.
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.add(R.id.fragment_container, fragment)
//
//        //transaction.add(R.id.fragment_container, WeatherPageFragment.newInstance("화창",10.0))
//        transaction.commit()
        val fragment = DustPageFragment.newInstance(37.579876,126.976998)

        // 프래그먼트 매너저의 add 메서드를 호출해서 생성한 날씨 프래그먼트를 추가합니다.
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragment_container, fragment)

        //transaction.add(R.id.fragment_container, WeatherPageFragment.newInstance("화창",10.0))
        transaction.commit()

    }
}