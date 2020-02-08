package brandon.backend.sos.filesystem

import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class StoredFileInputStream constructor(
        private val files: List<String>
): InputStream() {

    private val inputStreams = sequence {for(f in files) yield(FileInputStream(File(f)))}.iterator()

    var currentFile: FileInputStream? = null

    override fun read(): Int {
        if(currentFile==null && !inputStreams.hasNext()) return -1
        else if(currentFile == null){
            currentFile = inputStreams.next()
        }
        if(currentFile!!.available() == 0){
            if(inputStreams.hasNext()) currentFile = inputStreams.next()
            else return -1
        }
        return currentFile!!.read()
    }

}