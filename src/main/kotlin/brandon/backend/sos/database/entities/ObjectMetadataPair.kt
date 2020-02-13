package brandon.backend.sos.database.entities

import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
class ObjectMetadataPair constructor(

        @EmbeddedId
        val objectKeyPair: ObjectMetadataKey? = null,

//        @NotNull
//        @ManyToOne
//        val fileObj: FileObject? = null,
//        @NotNull
//        val key: String? = null,
        @NotNull
        val value: String? = null
) {

    constructor(fileObj: FileObject, key: String, value: String): this(
            ObjectMetadataKey(fileObj,key), value
    )

}