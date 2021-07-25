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
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URL
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class DustPageFragment : Fragment() {

    private val APP_TOKEN = ""

    lateinit var statusImage : ImageView
    lateinit var pm25StatusText : TextView
    lateinit var pm25IntensityText : TextView
    lateinit var pm10StatusText : TextView
    lateinit var pm10IntensityText : TextView

    fun startAnimation() {
        val fadeIn = AnimationUtils.loadAnimation(activity, R.anim.fade_in)
        statusImage.startAnimation(fadeIn)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.dust_page_fragment, container, false)
        // 정보를 보여줄 뷰들을 모두 onCreateView 메서드에서 초기화 합니다.
        statusImage = view.findViewById<ImageView>(R.id.dust_status_icon)
        pm25StatusText = view.findViewById<TextView>(R.id.dust_pm25_status_text)
        pm25IntensityText = view.findViewById<TextView>(R.id.dust_pm25_intensity_text)
        pm10StatusText = view.findViewById<TextView>(R.id.dust_pm10_status_text)
        pm10IntensityText = view.findViewById<TextView>(R.id.dust_pm10_intensity_text)

        return view

    }

    companion object{
        // 경도의 값을 번들 객체에 추가하고 프래그먼트를 생성해서 반환하는 newInstance 함수를 정의합니다.
        fun newInstance(lat : Double, lon: Double) :DustPageFragment {
            val fragment = DustPageFragment()

            val args = Bundle()
            args.putDouble("lat",lat)
            args.putDouble("lon",lon)
            fragment.arguments = args

            return fragment
        }
    }
    // DustCheckRespone 클래스를 정의하며 pm10, pm25를 저장할 속성을 추가합니다.
    @JsonDeserialize(using=DustCheckerResponseDeserializer::class)
    data class DustCheckResponse(val pm10: Int?, val pm25: Int?, val pm10Status : String, val pm25Status : String)

    // 역직렬화를 담당할 클래스를 정의합니다.
    class DustCheckerResponseDeserializer : StdDeserializer<DustCheckResponse>(DustCheckResponse::class.java) {
        private val checkCategory = { aqi : Int? -> when(aqi) { // 미세먼지 농도 값을 받아 상태 정보에 대한 문자열을 반환하는 람다 함수를 정의합니다.
            null -> "알 수 없음"
            in (0 .. 100) -> "좋음"
            in (101 .. 200) -> "보통"
            in (201 .. 300) -> "나쁨"
            else -> "매우 나쁨"
        }}

        // deserialize 메서드를 정의하고 역직렬화 코드를 작성합니다.
        // 먼저 최상위 노드에서 get메서드를 호출해서 data 객체에 접근하고, 계속해서 중첩되어 정의된 내부 객체에 접근하는 식으로 pm10, pm25 객체에 접근합니다.
        override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): DustCheckResponse {
            var node : JsonNode? = p?.codec?.readTree<JsonNode>(p)

            var dataNode : JsonNode? = node?.get("data")
            var iaqiNode = dataNode?.get("iaqi")
            var pm10Node = iaqiNode?.get("pm10")
            var pm25Node = iaqiNode?.get("pm25")
            var pm10 = pm10Node?.get("v")?.asInt()
            var pm25 = pm25Node?.get("v")?.asInt()

            // (2)
            var pm10Status = checkCategory(pm10)
            var pm25Status = checkCategory(pm25)

            // 미세먼지 농도를 포함하고 있는 객체를 생성해서 반환합니다.
            return DustCheckResponse(pm10, pm25, pm10Status, pm25Status)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lat = arguments!!.getDouble("lat")
        val lon = arguments!!.getDouble("lon")

//        // 번들 객체에서 위도, 경도 정보를 받아와 웹 API 서비스를 요청할 URL을 완성함니다.
//        val url = "http://api.waqi.info/feed/geo:${lat};${lon}/?token=${APP_TOKEN}"

        val retrofit = Retrofit.Builder()
            .baseUrl("http://api.waqi.info")
            .addConverterFactory(
                GsonConverterFactory.create(
                    // (2)
                    GsonBuilder().registerTypeAdapter(
                        DustCheckResponseFromGSON::class.java,
                        DustCheckerResponseDeserializerGSON()
                    ).create()
                ))
            .build()

        // (3)
        val apiService = retrofit.create(DustCheckAPIService::class.java)
        val apiCallForData = apiService.getDustStatusInfo(lat, lon, APP_TOKEN)

        apiCallForData.enqueue(object : Callback<DustCheckResponseFromGSON> {
            override fun onFailure(call: Call<DustCheckResponseFromGSON>, t: Throwable) {
                Toast.makeText(activity, "에러 발생 : ${t.message}", Toast.LENGTH_SHORT).show()
            }

            // (4)
            override fun onResponse(call: Call<DustCheckResponseFromGSON>, response: Response<DustCheckResponseFromGSON>) {
                val data = response.body()

                if(data != null) {
                    // (1)
                    statusImage.setImageResource(when(data.pm25Status) {
                        "좋음" -> R.drawable.good
                        "보통" -> R.drawable.normal
                        "나쁨" -> R.drawable.bad
                        else -> R.drawable.very_bad
                    })

                    pm25IntensityText.text = data.pm25?.toString() ?: "알 수 없음"
                    pm10IntensityText.text = data.pm10?.toString() ?: "알 수 없음"


                    // (2)
                    pm25StatusText.text = "${data.pm25Status} (초미세먼지)"
                    pm10StatusText.text = "${data.pm10Status} (미세먼지)"
                }
            }
        })
    }
}
