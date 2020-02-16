package brandon.backend.sos.rest.async

import org.springframework.http.HttpStatus

abstract class Request constructor(val bufferSize: Int = 20000) {

    abstract fun hasMoreData(): Boolean
    abstract fun readData(): ByteArray
    abstract fun writeData(data: ByteArray)
    abstract fun completeRequest(status: HttpStatus)

}