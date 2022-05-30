
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.redundent.kotlin.xml.Node

@Serializable
data class Person(var name: String = "", var age: Int = 0, val gender: Gender = Gender.NotDefined):
    java.io.Serializable {

    constructor(name: String, age: Int, gender: String): this(
        name,
        age,
        when (gender) {
            "М", "M", "m", "м", Gender.Male.name -> Gender.Male
            "Ж", "F", "f", "ж", Gender.Female.name -> Gender.Female
            else -> Gender.NotDefined
        }
    )

    companion object {
        fun fromJson(jsonString: String): Person =
            Json.decodeFromString(serializer(), jsonString)

        //возвращает объект класса Person из xml строки
        fun fromString(string: String): Person {
            val nameIndex = string.indexOf("name=") + 5
            val ageIndex = string.indexOf("age=") + 4
            val genderIndex = string.indexOf("gender=") + 7

            val name = string.substring(nameIndex, string.indexOf(',', nameIndex))
            val age = string.substring(ageIndex, string.indexOf(',', ageIndex)).toInt()
            val gender = string.substring(genderIndex, string.length - 1)

            return Person(name, age, gender)
        }
    }

    fun toJson() =
        Json.encodeToJsonElement(serializer(), this)

    operator fun get(property: String) = when(property) {
        "name" -> name
        "age" -> age.toString()
        "gender" -> gender.name
        else -> null
    }

    fun toXml(node: Node) = node.element("Person") {
        attribute("name", name)
        attribute("age", age)
        attribute("gender", gender.name)
    }

    fun toPrintString() =
        "name = $name, age = $age, gender = $gender"
}



enum class Gender {
    Male,
    Female,
    NotDefined
}