package brandon.backend.sos.filesystem.io

import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class StoredFileInputStream constructor(
        private val files: List<String>
): InputStream() {

    private val inputStreams = sequence {for(f in files) yield(FileInputStream(File(f)))}.iterator()

    private var currentFile: FileInputStream? = null

    private fun pointToNextData(){
        while((currentFile == null || currentFile!!.available() == 0) && inputStreams.hasNext()) currentFile = inputStreams.next()
    }

    override fun available(): Int {
        pointToNextData()
        return if(currentFile == null) 0
        else currentFile!!.available()
    }

    override fun read(): Int {
        pointToNextData()
        return if(currentFile == null) -1
        else currentFile!!.read()
    }

}