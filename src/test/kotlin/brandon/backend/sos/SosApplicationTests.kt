package brandon.backend.sos

import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
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
			createBucket("test")
		}
		catch(e:Exception){
			assert(false) { "Could not create bucket, ${e.message}" }
		}
		try {
			createBucket("test")
			assert(false) { "Should not be able to create bucket" }
		}catch(e:Exception){
		}
		try {
			createBucket(".test")
			assert(false) { "Should not be able to create bucket" }
		}catch(e:Exception){
		}

		try{
			val listReq = sendRequest("GET","/test?list")
			println(listReq)
			assert(listReq.contains("\"name\"\\s*:\\s*\"test\"".toRegex()))
		}
		catch(e:Exception){
			assert(false)
		}

		try {
			val deleteBucket = sendRequest("DELETE", "/test?delete")
			println(deleteBucket)
		}catch(e: Exception){
			assert(false)
		}
	}

	fun createBucket(bucketName: String){
		val createResp = sendRequest("POST", "/${bucketName}?create")
		println(createResp)
		assert(createResp.contains("\"name\"\\s*:\\s*\"${bucketName}\"".toRegex()))
	}

	@Test
	fun ticketTest(){
		createBucket("test")

		try{
			val createTicket = sendRequest("POST","/test/test.pdf?create")
		}catch(e: Exception){assert(false) { "Should be able to create a ticket" }}
		try{
			val createTicket = sendRequest("POST","/test/test.pdf?create")
			assert(false){ "Shouldn't be able to create a duplicate ticket" }
		}catch(e: Exception){}
		try{
			val createTicket = sendRequest("POST","/test121/test.pdf?create")
			assert(false) { "Shouldn't be able to create a ticket for an object that doesn't exist" }
		}catch(e: Exception){}
		try{
			val createTicket = sendRequest("POST","/test/.test.pdf.?create")
			assert(false) { "Shouldn't be able to create a ticket for an invalid object name" }
		}catch(e: Exception){}

		val file1 = File("src/main/resources/xaa")
		val file2 = File("src/main/resources/xab")
		val file3 = File("src/main/resources/test")
		uploadFile(file1,"/test/test.pdf",1)
		uploadFile(file2,"/test/test.pdf",2)
		uploadFile(file3,"/test/test.pdf",3)
		try{
			val completeTicket = sendRequest("POST","/test/test.pdf?complete")
			println(completeTicket)
		}catch(e: Exception){
			assert(false){"Shouldve been able to complete ticket: ${e.message}"}
		}
		try{
			val buckets = sendRequest("GET","/test?list")
			assert(buckets.contains("test.pdf"))
		}
		catch(e: Exception){assert(false){"Should be able to retrieve bucket 'test'"}}
	}

	fun getMd5(file: File): String{
		val md = MessageDigest.getInstance("MD5")
		md.reset()
		val inputStream = file.inputStream()
		while(inputStream.available() > 0){
			md.update(inputStream.readBytes())
		}
		return DatatypeConverter.printHexBinary(md.digest()).toUpperCase()
	}

	fun uploadFile(file: File,path: String,partNo: Int){
		val md5 = getMd5(file)
		println("Md5 for file: ${file.name} is $md5")
		val client = HttpClients.createDefault()
		val put = HttpPut("$url$path?partNumber=$partNo")
		put.setHeader("Content-MD5",md5)
		put.allHeaders.forEach { println(it) }
		val builder = MultipartEntityBuilder.create()
		builder.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM,file.name)
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
