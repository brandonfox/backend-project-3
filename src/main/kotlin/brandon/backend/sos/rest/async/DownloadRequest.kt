package brandon.backend.sos.rest.async

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.async.DeferredResult
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min

class DownloadRequest(
        private val inputStream: InputStream,
        private val outputStream: OutputStream,
        private val result: DeferredResult<ResponseEntity<Any>>,
        range: String? = null
) : Request() {

    var bytesToRead: Int = Int.MAX_VALUE

    init {
        if(range != null){
            val unit = range.substringBefore('=')
            if(unit != "bytes") throw Exception("Range unit must be bytes")
            val start = range.substringAfter('=').substringBefore('-').toInt()
            val end = range.substringAfter('-').toInt()
            bytesToRead = end - start
            inputStream.skip(start.toLong())
        }
    }

    override fun hasMoreData(): Boolean {
        return inputStream.available() > 0 && bytesToRead > 0
    }

    override fun readData(): ByteArray {
        val toRead = min(bufferSize,bytesToRead)
        val byteAr = ByteArray(toRead)
        inputStream.read(byteAr)
        bytesToRead -= toRead
        return byteAr
    }

    override fun writeData(data: ByteArray) {
        outputStream.write(data)
    }

    override fun completeRequest(status: HttpStatus) {
        inputStream.close()
        outputStream.close()
        result.setResult(ResponseEntity(status))
    }
}