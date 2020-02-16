package brandon.backend.sos

import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter
import kotlin.Exception

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SosApplicationTests {

	private val url = "http://localhost:8080"


	@BeforeAll
	internal fun resetAll(){
		try {
			sendRequest("DELETE", "/test?delete")
		}catch(e: Exception){}
	}

	@Test
	fun bucketTest() {

		try {
			createBucket()
		}
		catch(e:Exception){
			assert(false) { "Could not create bucket, ${e.message}" }
		}
		try {
			createBucket()
			assert(false) { "Should not be able to create bucket" }
		}catch(e:Exception){
			assert(true) { "Tried to create bucket return error message" }
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

		val file = File("src/main/resources/Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein - Introduction to Algorithms-MIT Press (2009).pdf")
		uploadFile(file,"/test/test",1)
	}

	fun getMd5(file: File): String{
		val md = MessageDigest.getInstance("MD5")
		file.forEachLine { md.digest(it.toByteArray()) }
		return DatatypeConverter.printHexBinary(md.digest()).toUpperCase()
	}

	fun uploadFile(file: File,path: String,partNo: Int){
		val md5 = getMd5(file)
		val client = HttpClients.createDefault()
		val put = HttpPut("$url$path?partNumber=$partNo")
		put.addHeader("Content-MD5",md5)
		val builder = MultipartEntityBuilder.create()
		builder.addBinaryBody("file", file, ContentType.MULTIPART_FORM_DATA,file.name)
		val multipart = builder.build()
		put.entity = multipart
		val resp = client.execute(put)
		val response = resp.entity
		println(response.content.reader().readText())
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
