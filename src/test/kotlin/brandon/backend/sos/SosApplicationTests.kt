package brandon.backend.sos

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.net.HttpURLConnection
import java.net.URL
import kotlin.Exception

@SpringBootTest
class SosApplicationTests {

	private val url = "http://localhost:8080"

	@Test
	fun bucketTest() {

		try {
			createBucket()
		}
		catch(e:Exception){
			assert(false)
		}
		try {
			createBucket()
			assert(false)
		}catch(e:Exception){
			assert(true)
		}

		try{
			val listReq = sendRequest("GET","/test?list")
			println(listReq)
			assert(listReq.contains("\"name\"\\s*:\\s*\"test\"".toRegex()))
		}
		catch(e:Exception){

		}

		try {
			val deleteBucket = sendRequest("DELETE", "/test?delete")
			println(deleteBucket)
			assert(true)
		}catch(e: Exception){
			assert(false)
		}
	}

	fun createBucket(){
		val createResp = sendRequest("POST", "/test?create")
		println(createResp)
		assert(createResp.contains("\"name\"\\s*:\\s*\"test\"".toRegex()))
	}

	@Test
	fun ticketTest(){
		createBucket()

		try{
			val createTicket = sendRequest("POST","/test/test?create")
			assert(true)
		}catch(e: Exception){assert(false)}

	}

	fun sendRequest(type: String, path: String, headers: Map<String,String>? = null): String{
		val u = URL("$url$path")
		with(u.openConnection() as HttpURLConnection){
			requestMethod = type
			if(headers != null) {
				for (k in headers.keys) {
					setRequestProperty(k,headers[k])
				}
			}

			return inputStream.reader().readText()
		}
	}

}
