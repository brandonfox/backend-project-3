package brandon.backend.sos.rest.async

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.async.DeferredResult
import java.io.InputStream
import java.io.OutputStream

class DownloadRequest(
        private val inputStream: InputStream,
        private val outputStream: OutputStream,
        private val result: DeferredResult<ResponseEntity<Any>>
) : Request() {

    override fun hasMoreData(): Boolean {
        return inputStream.available() > 0
    }

    override fun readData(): ByteArray {
        inputStream.read(byteArray)
        return byteArray
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