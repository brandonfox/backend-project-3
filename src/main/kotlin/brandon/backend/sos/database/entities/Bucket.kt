package brandon.backend.sos.database.entities

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
class Bucket constructor(
        @Id
        @Column(name="bucket_name")
        val name: String? = null,
        @NotNull
        val created: Long? = null,
        @NotNull
        val modified: Long? = null,
        @OneToMany(mappedBy = "bucket" ,cascade = [CascadeType.ALL])
        @JsonManagedReference
        var objects: Set<FileObject> = HashSet()
) : Serializable {



}