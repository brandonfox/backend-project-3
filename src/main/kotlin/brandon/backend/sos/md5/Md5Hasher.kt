package brandon.backend.sos.md5

import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

object Md5Hasher {
    fun getMd5Digest(): MessageDigest {
        return MessageDigest.getInstance("MD5")
    }

    fun getMd5Hash(digest: MessageDigest): String{
        val mdigest = digest.digest()
        return DatatypeConverter.printHexBinary(mdigest).toUpperCase()
    }
}