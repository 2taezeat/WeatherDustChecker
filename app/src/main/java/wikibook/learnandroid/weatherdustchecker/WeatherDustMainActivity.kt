package wikibook.learnandroid.weatherdustchecker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager

class WeatherDustMainActivity : AppCompatActivity() {
    private lateinit var mPager: ViewPager
    private var lat: Double = 0.0
    private var lon: Double = 0.0

    // (2) 위치 정보를 얻기 위해 사용할 객체와 획득한 위치 정보를 활용할 코드를 작성하는 데 필요한 객체를 추가함.
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private val PERMISSION_REQUEST_CODE : Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_dust_main_activity)
        supportActionBar?.hide()

        mPager = findViewById(R.id.pager)
        // (4) 위치 관리자와 관련된 상수(Context.LOCATION_SERVICE) 를 전달해 LocationManager 객체를 얻어 옵니다. 이 메서드에서는 Any 타입의 객체를 반환하므로 위치 매니저 타입으로 변환할 필요가 있습니다.
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager


        // (5)
        locationListener = object : LocationListener {
            // (6)
            override fun onLocationChanged(location: Location) {
                lat = location.latitude
                lon = location.longitude

                // (7) 위치 정보는 앱을 실행한 이후 한번만 받아오면 충분함.
                locationManager.removeUpdates(this)

                // (8) 위치 정보를 받아온 시점 이후 해당 위치 정보를 활용해 ViewPager 뷰를 초기화하고 리스너도 설정합니다.
                val pagerAdapter = MyPagerAdapter(supportFragmentManager)
                mPager.adapter = pagerAdapter
                mPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageScrollStateChanged(p0: Int) {}
                    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}
                    override fun onPageSelected(position: Int) {
                        if(position == 0) {
                            // (1) 어댑터 객체에서 제공하는 instantiateItem 메서드에 ViewPager 객체와 현재 위치를 전달해서 프래그먼트를 전달받고 곧바로 startAnimateion 메서드를 호출해 에니메이션 효과를 적용합니다.
                            val fragment = mPager.adapter?.instantiateItem(mPager, position) as WeatherPageFragment
                            fragment.startAnimation()
                        } else if(position == 1) {
                            // 미세먼지 정보 프래그먼트로 변경된 시점에도 같은 방식으로 startAnimation 메서드를 호출
                            val fragment = mPager.adapter?.instantiateItem(mPager, position) as DustPageFragment
                            fragment.startAnimation()
                        }
                    }
                })
            }
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        // (9) ContextCompat 클래스의 checkSelfPermission 함수를 호출해 위치 정보 접근과 관련된 권한 획득이 이뤄졌는지 여부를 확인합니다.
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // (10) 위치 정보에 접근할 수 있따면 본격적으로 위치 정보를 받아오기 위해 위치 매니저 객체에서 제공하는 requestLocationUpdates 메서드를 호출합니다.
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
        } else {
            // (11) 위치 권한에 대한 요청이 허락되지 않은 상황이라면, 권한을 요청하는 함수를 호출함.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        }
    }

    // (12) 권한에 대한 요청을 수락하거나 거절한 시점 이후에 호출되는 콜백 메서드를 재정의합니다.
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when(requestCode) {
            // (13)
            PERMISSION_REQUEST_CODE -> {
                var allPermissionsGranted = true
                for(result in grantResults) {
                    allPermissionsGranted = (result == PackageManager.PERMISSION_GRANTED) // 모든 권한이 허용되었는지 여부 조사
                    if(!allPermissionsGranted) break // 권한에 대해 동의하지 않았다고 확인될 경우 바로 반복문 탈출
                }
                if(allPermissionsGranted) {
                    // (14) 모든 권한 승인이 이뤄졌으므로 메서드를 호출하여 리스너 객체를 전달해서 단말기 위치 탐색을 시작합니다.
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
                } else {
                    // (15) 권한 승인이 이뤄지지 않았다면 더는 진행할 수 있는 작업이 없으므로, 토스트 메시지를 통해 사실을 공지한 후, 액티비티 종료함.
                    Toast.makeText(applicationContext, "위치 정보 제공 동의가 필요합니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }


    // MyPagerAdapter 클래스를 정의합니다. 이 클래스는 프래그먼트 매니저 객체를 필요로 하는
    private inner class MyPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        // (4) 총 페이지(프래그먼트)의 개수를 구하는 getCOount 메서드를 재정의합니다. (날씨, 미세먼지 총 2개)
        override fun getCount(): Int = 2
        // (5) 페이지의 위치에 따라 프래그먼트 객체를 반환하는 역할을 수행하는 getItem 메서드를 재정의 합니다.
        override fun getItem(position: Int): Fragment {
            return when(position) {
                0 -> WeatherPageFragment.newInstance(lat, lon)
                1 -> DustPageFragment.newInstance(lat, lon)
                else -> {
                    throw Exception("페이지가 존재하지 않음.")
                }
            }
        }
    }


}