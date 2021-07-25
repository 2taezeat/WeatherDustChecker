package wikibook.learnandroid.weatherdustchecker

import java.net.URL
import java.net.HttpURLConnection
import android.os.AsyncTask

// API 요청을 처리하기 위해 클래스를 정의하며 생성자로 onPostExecute 메서드에서 호출할 콜백 메서드를 구현한 인터페이스 객체를 전달받음.
class APICall (val callback: APICall.APICallback) : AsyncTask<URL, Void, String>() {
    // 필요한 콜백 메서드를 포함한 인터페이스를 정의합니다. 해당 인터페이스를 구현한 객체는 호스트 액티비티에서 APICall 클래스 객체를 생성하는 과정에서 전달합니다.
    interface APICallback {
        fun onComplete(result : String)
    }

    override fun doInBackground(vararg params: URL?): String {

        val url = params.get(0)
        val conn : HttpURLConnection = url?.openConnection() as HttpURLConnection
        conn.connect() // connent 메서드를 호출해서 HTTP 요청 메서지를 전송합니다.

        // 연결 객체에서 HTTP 응답 메시지의 상태 코드 값에 즙근해 상태 코드가 200으로 정상적인 응답 코드가 반환되었는지 확인합니다. 이후 연결 객체의 입력 스트림과
        // BufferedReader 객체를 이용해 응답 메시지에 포함된 문자열 데이터를 읽어 옵니다.
        var body = conn?.inputStream.bufferedReader().use { it.readText() }

        conn.disconnect()

        return body // 응답 메시지의 내용은 날씨 정보를 포함하고 있는 JSON 문자열이 될 것임.
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        // 반환 받은 JSON 문자열을 콜백 메서드를 통해 처리하도록 전달합니다. 전달한 JSON 문자열은 특정 클래스의 객체로 변환하고 정보를 추출하는 작업은 액티비티나 프래그먼트 쪽에서 전달한 콜백 메서드를 통해 처리합니다.
        if (result != null)  {
            callback.onComplete(result) }
    }
}



