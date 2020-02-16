package brandon.backend.sos

import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
import java.io.FileOutputStream
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
		deleteBucket("test")
		createBucket("test",true,"Should be able to create bucket")
		createBucket("test",false,"Shouldnt be able to create duplicate bucket")
		createBucket("test",false,"Shouldnt be able to create bucket with invalid name")
		createBucket("test",false,"Shouldnt be able to create bucket with invalid name")
	}

	fun deleteBucket(bucketName: String){
		try {
			sendRequest("DELETE", "/$bucketName?delete")
		}catch(e:Exception){}
	}

	fun createBucket(bucketName: String, shouldSuccess: Boolean, errormsg: String = ""){
		if(shouldSuccess)
			sendRequest("POST","/$bucketName?create",shouldSuccess,errormsg,{it.contains("\"name\"\\s*:\\s*\"${bucketName}\"".toRegex())})
		else
			sendRequest("POST","/$bucketName?create",shouldSuccess,errormsg)
	}

	fun createTicket(bucketName: String,objectName: String, shouldSuccess: Boolean, errormsg: String){
		sendRequest("POST","/$bucketName/$objectName?create",shouldSuccess = shouldSuccess,errormsg = errormsg)
	}

	@Test
	fun ticketTest(){
		deleteBucket("test")
		createBucket("test",true,"Should be able to create bucket")
		createTicket("test","test.pdf",true,"Should be able to create a ticket")
		createTicket("test","test.pdf",false,"Shouldn't be able to create a duplicate ticket")
		createTicket("test121","test.pdf",false,"Shouldn't be able to create a ticket for an object that doesn't exist")
		val file1 = File("src/main/resources/xaa")
		val file2 = File("src/main/resources/xab")
		val file3 = File("src/main/resources/test")
		createObject("test","test.pdf",false,file1,file2,file3)
		sendRequest("GET","/test?list",shouldSuccess = true,errormsg = "Should be able to retrieve bucket 'test'",assertFunction = {it.contains("test.pdf")})
		sendRequest("DELETE","/test/test.pdf?delete",true,"Should be able to delete object")
		sendRequest("GET","/test?list",true,"Object should no longer show up in bucket",{!it.contains("test.pdf")})
	}

	@Test
	fun objectDownloadTest(){
		deleteBucket("test")
		createBucket("test",true,"Should be able to create bucket")
		val file1 = File("src/main/resources/xaa")
		val file2 = File("src/main/resources/xab")
		createObject("test","test.pdf",true,file1,file2)
		val file = File("src/main/resources/downloads/test.pdf")
		file.createNewFile()
		downloadFile("test","test.pdf",file)
		assert(file.length() > 5000)
	}

	fun createObject(bucketName: String,objectName: String,createTicket: Boolean = true,vararg files: File){
		if(createTicket)
			createTicket(bucketName,objectName,true,"Should be able to create ticket")
		files.forEachIndexed  { i,f -> run { uploadFile(f, "/$bucketName/$objectName", i+1) } }
		sendRequest("POST","/$bucketName/$objectName?complete",shouldSuccess = true,errormsg = "Should've been able to complete ticket")
	}

	@Test
	fun metadataTest(){
		deleteBucket("test")
		createBucket("test",true,"Should be able to create bucket")
		val file1 = File("src/main/resources/xaa")
		val file2 = File("src/main/resources/xab")
		createObject("test","test.pdf",true,file1,file2)
		putMetadata("test","test.pdf","origin","Spring JUnit test")

	}

	fun downloadFile(bucketName: String,objectName: String,file: File){
		val u = URL("$url/$bucketName/$objectName")
		val fileStream = FileOutputStream(file)
		with(u.openConnection() as HttpURLConnection){
			requestMethod = "GET"

			println(responseCode)
			while(inputStream.available() > 0)
				fileStream.write(inputStream.read())
		}
	}

	fun putMetadata(bucketName: String,objectName: String,key: String, body: String): String{
		val client = HttpClients.createDefault()
		val put = HttpPut("$url/$bucketName/$objectName?metadata&key=$key")
		val ent = StringEntity(body)
		put.entity = ent
		val respEnt = client.execute(put)
		return respEnt.entity.content.reader().readText()
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

	fun sendRequest(type: String, path: String,body: String? = null,headers: Map<String,String>? = null): String{
		val u = URL("$url$path")
		with(u.openConnection() as HttpURLConnection){
			requestMethod = type
			if(headers != null) {
				for (k in headers.keys) {
					setRequestProperty(k,headers[k])
				}
			}
			if(body != null){
				doOutput = true
				outputStream.writer().write(body)
			}
			println(responseCode)
			return inputStream.reader().readText()
		}
	}
	fun sendRequest(type: String, path: String, shouldSuccess: Boolean,errormsg: String, assertFunction: ((resp: String) -> Boolean)? = null,body: String? = null,headers: Map<String,String>? = null): String{
		val asf: (resp: String) -> Boolean = assertFunction ?: {true}
		return try{
			val response = sendRequest(type,path,body,headers)
			if(shouldSuccess) assert(asf(response)){errormsg}
			else assert(false){errormsg}
			response
		}catch(e: Exception){
			if(shouldSuccess) assert(false){"$errormsg:${e.message}"}
			else assert(asf(e.message!!)){"$errormsg:${e.message}"}
			e.message!!
		}
	}

}
