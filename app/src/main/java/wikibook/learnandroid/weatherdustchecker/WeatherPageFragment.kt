package wikibook.learnandroid.weatherdustchecker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.net.URL

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherPageFragment : Fragment(){

    // 날씨 데이터를 포함한 OpenWeatherAPIJSONResponse 클래스를 정의합니다.
    @JsonIgnoreProperties(ignoreUnknown=true)
    data class OpenWeatherAPIJSONResponse(val main: Map<String, String>, val weather: List<Map<String, String>>)

    private val APP_ID = ""

    // 뷰 객체 참조용을 클래스의 속성 추가
    lateinit var weatherImage : ImageView
    lateinit var statusText : TextView
    lateinit var temperatureText : TextView

    companion object{
        // 프래그먼트에 필요한 정보를 전달하기 위해 newInstance 함수를 정의하고 함수 내부에서 프래그먼트를 생성합니다. 동시에 전달받은 인자
        // 를 번들 객체에 추가합니다.
        fun newInstance(lat: Double, lon:Double) : WeatherPageFragment {
            val fragment = WeatherPageFragment()

            val args = Bundle()
            args.putDouble("lat", lat)
            args.putDouble("lon", lon)
            //args.putInt("res_id", R.drawable.sun) // 이미지 리소스 식별자 추가
            fragment.arguments = args

            return fragment

        }
    }

    fun startAnimation() {
        // (1) Context 객체와 애니메이션 리소스 식별자를 전달해서 Animation 타입의 객체를 반환받습니다.
        val fadeIn = AnimationUtils.loadAnimation(activity, R.anim.fade_in)
        // (2) 불러온 애니메이션 객체(fadeIn)를 뷰의 startAnimation 메서드를 호출할 때 전달해서 뷰의 에니메이션을 적용합니다.
        weatherImage.startAnimation(fadeIn)
    }


    override fun onCreateView(inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle?): View{
        val view = inflater.inflate(R.layout.weather_page_fragment,container,false)

        weatherImage = view.findViewById<ImageView>(R.id.weather_icon)
        statusText = view.findViewById<TextView>(R.id.weather_status_text)
        temperatureText = view.findViewById<TextView>(R.id.weather_temp_text)


        // newInstance 함수에서 추가한 번들 객체에 접근하고 전달받은 데이터를 이용해 뷰 정보를 설정합니다.
//        weatherImage.setImageResource(arguments!!.getInt("res_id"))
//        statusText.text = arguments!!.getString("status")
//        temperatureText.text = "${arguments!!.getDouble("temperatue")}"

        return view
    }

    // 뷰가 모두 생성된 시점 이후에 호출되는 onViewCreated 메서드를 재정의해서 번들 객체로부터 전달받은 위치 정보를 추출합니다.
    // 이후 앞서 정의한 APICall 클래스의 객체를 생성하고 콜백 메서드를 구현해서 네트워크 요청을 보내고 결과를 로그 메시지로 출력하도록 구현합니다.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 번들 객체에서 가져온 위도와 경도 정보 및 API 키를 이용해 최종 요청 주소를 생성합니다.
        val lat = arguments!!.getDouble("lat")
        val lon = arguments!!.getDouble("lon")


        // (1)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://api.openweathermap.org")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // (2)
        val apiService = retrofit.create(WeatherAPIService::class.java)

        // (3) 인터페이스에 정의한 주소 접근 메서드를 호출하며 주소의 쿼리 스트링에 포함될 API 키와 위치 저보를 전달합니다.
        val apiCallForData = apiService.getWeatherStatusInfo(APP_ID, lat, lon)
        // (4)
        apiCallForData.enqueue(object : Callback<OpenWeatherAPIJSONResponseFromGSON> {
            override fun onFailure(call: Call<OpenWeatherAPIJSONResponseFromGSON>, t: Throwable) {
                // (5)
                Toast.makeText(activity, "에러 발생 : ${t.message}", Toast.LENGTH_SHORT).show()
            }


            override fun onResponse(
                call: Call<OpenWeatherAPIJSONResponseFromGSON>,
                response: Response<OpenWeatherAPIJSONResponseFromGSON>
            ) {
                // (6)
                val data = response.body()

                if (data != null) {
                    val temp = data.main.get("temp")
                    temperatureText.text = temp

                    val id = data.weather[0].get("id")
                    if (id != null) {
                        statusText.text = when {
                            id.startsWith("2") -> {
                                weatherImage.setImageResource(R.drawable.flash)
                                "천둥, 번개"
                            }
                            id.startsWith("3") -> {
                                weatherImage.setImageResource(R.drawable.rain)
                                "이슬비"
                            }
                            id.startsWith("5") -> {
                                weatherImage.setImageResource(R.drawable.rain)
                                "비"
                            }
                            id.startsWith("6") -> {
                                weatherImage.setImageResource(R.drawable.snow)
                                "눈"
                            }
                            id.startsWith("7") -> {
                                weatherImage.setImageResource(R.drawable.cloudy)
                                "흐림"
                            }
                            id.equals("800") -> {
                                weatherImage.setImageResource(R.drawable.sun)
                                "화창"
                            }
                            id.startsWith("8") -> {
                                weatherImage.setImageResource(R.drawable.cloud)
                                "구름 낌"
                            }
                            else -> "알 수 없음"
                        }
                    }
                }
            }
        })
    }
}










