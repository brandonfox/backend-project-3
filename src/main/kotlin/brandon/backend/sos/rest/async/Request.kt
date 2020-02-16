package brandon.backend.sos.rest.async

import org.springframework.http.HttpStatus

abstract class Request constructor(bufferSize: Int = 20000) {

    val byteArray = ByteArray(bufferSize)

    abstract fun hasMoreData(): Boolean
    abstract fun readData(): ByteArray
    abstract fun writeData(data: ByteArray)
    abstract fun completeRequest(status: HttpStatus)

}