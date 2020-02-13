package brandon.backend.sos.database.entities

import java.io.Serializable
import javax.persistence.Embeddable
import javax.persistence.ManyToOne

@Embeddable
class ObjectMetadataKey constructor(
        @ManyToOne
        val fileObject: FileObject? = null,
        val metadataKey: String? = null
) : Serializable