package brandon.backend.sos.rest.async

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.lang.Exception
import java.util.concurrent.*

object AsyncController {

    private const val noThreads = 3

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val threadIdQueue = PriorityBlockingQueue<Int>()
    val asyncThreadRequests = HashMap<Int,BlockingQueue<Request>>()

    var lastAddedThread = 1

    init {
        for(i in 1 until noThreads + 1){
            threadIdQueue.add(i)
            asyncThreadRequests.putIfAbsent(i,LinkedBlockingQueue())
            AsyncHandler(i).start()
        }
    }

    fun addRequest(request: Request){
        asyncThreadRequests[lastAddedThread]!!.add(request)
        lastAddedThread = (lastAddedThread % noThreads) + 1
    }

    class AsyncHandler constructor(
            private val threadId: Int
    ) : Thread() {
        override fun run() {
            logger.info("Created async thread $threadId")
            while(true){
                val nextReq = asyncThreadRequests[threadId]!!.take()
                try {
                    if (nextReq.hasMoreData()) {
                        nextReq.writeData(nextReq.readData())
                        asyncThreadRequests[threadId]!!.put(nextReq)
                    } else {
                        logger.info("Completed a request ${nextReq.javaClass}")
                        nextReq.completeRequest(HttpStatus.OK)
                    }
                }catch(e: Exception){
                    logger.info("Request closed with status: ${e.message}")
                }
            }
        }
    }

}