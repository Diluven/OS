import kotlinx.serialization.json.JsonElement
import org.redundent.kotlin.xml.XmlVersion
import org.redundent.kotlin.xml.xml
import java.io.File.listRoots
import java.nio.file.Path
import java.util.*
import javax.swing.filechooser.FileSystemView
import kotlin.collections.ArrayList
import kotlin.io.path.*

val input = Scanner(System.`in`)

fun main() {
    println("Выберите команду из списка ниже и введите её номер")
    println("------Информация о дисках-----")
    println("1. Вывести информацию о дисках\n")
    println("-------Работа с файлами-------")
    println("2. Создать файл")
    println("3. Записать строку в файл")
    println("4. Вывести полное содержимое файла")
    println("5. Вывести определённую строку на экран")
    println("6. Удалить файл\n")
    println("--------Работа с JSON---------")
    println("7. Создать файл Person.json")
    println("8. Выполнить сериализацию и записать в файл")
    println("9. Вывести содержимое файла")
    println("10. Вывести конкретное свойство")
    println("11. Удалить файл\n")
    println("---------Работа с XML---------")
    println("12. Создать файл Persons.xml")
    println("13. Добавить данные в файл")
    println("14. Вывести полное содержимое файла")
    println("15. Вывести конкретный объект")
    println("16. Удалить файл")
    print(">>>  ")

    when (val cmd = input.nextInt()) {
        1 -> disksInfo()
        2, 7, 12 -> {
            val file = when (cmd) {
                2 -> File()
                7 -> Json()
                12 -> Xml()
                else -> File()
            }
            when {
                file.exists() -> println("Файл уже существует!")
                file.create().exists() -> println("Файл успешно создан!")
                else -> println("Не удалось создать файл")
            }
        }
        3 -> {
            val file = File()
            println("Введите текст для записи в файл")
            file.append(readLine() ?: "")
        }
        4 -> {
            val file = File()
            val content = file.readAll()

            if (content == null) println("Не удалось открыть файл")
            else println(
                """
                |Содержимое файла "${file.filename}":
                |${file.readAll()}
                """.trimMargin()
            )
        }
        5 -> {
            println("Введите номер строки (начиная с 1)")
            val numberLine = input.nextInt()
            val file = File()
            val line = file.readLine(numberLine)

            if (line == null) println("Не удалось открыть файл")
            else println(
                """
                |$line строка файла "${file.filename}":
                |${file.readLine(numberLine - 1)}
                |""".trimMargin()
            )
        }
        6, 11, 16 -> {
            val file = when(cmd) {
                6 -> File()
                11 -> Json()
                16 -> Xml()
                else -> File()
            }

            if (file.delete())
                println("Удаление завершено")
            else
                println("Не удалось удалить файл")
        }
        8 -> {
            val person = createPerson()
            val json = Json()

            json.write(person.toJson())

            println("Данные успешно сериализированы и записаны в файл ${json.filename}!")
        }
        9 -> {
            val string = Json().readAll()

            if (string == null) {
                println("Не удалось открыть файл")
                return
            }
            val person = Person.fromJson(string)
            println(person.toString())
        }
        10 -> {
            println("Введите название свойства класса Person (name, age или gender)")
            val property = input.next().lowercase()

            println(Json().readProperty(property) ?: "Не удалось открыть файл")
        }
        13 -> {
            val person = createPerson()
            Xml().write(person)
            println("Данные записаны в файл!")
        }
        14 -> println(Xml().readAll())
        15 -> {
            println("Введите имя, возраст или пол человека")
            val key = input.next()
            println("Найденные персоны:")
            Xml().readPerson(key).forEach {
                println(it.toPrintString())
            }
        }
    }
}

fun createPerson(): Person {
    println("Создание объекта Person:")
    print("Введите имя  >>>  ")
    val name = input.next()
    print("Введите возраст  >>>  ")
    val age = input.nextInt()
    print("Введите пол (М - мужской, Ж - женский)  >>>  ")
    val genderString = input.next()

    return Person(name, age, genderString)
}



fun disksInfo() {
    val fsv = FileSystemView.getFileSystemView()
    val roots = listRoots()

    roots.forEach {
        println("Название: $it")
        println("Тип: ${fsv.getSystemTypeDescription(it)}")
        println("Объём диска: ${it.totalSpace} байт")
        println("Свободное пространство: ${it.freeSpace} байт")
    }
}




open class File(name: String = "") {

    val filename: String = if (name.isEmpty()) {
        println("Введите имя файла")
        input.next()
    } else name

    private val filePath = //File::class.java.classLoader.getResource(filename)?.path?.substring(1) ?:
        "C:\\Users\\petro\\IdeaProjects\\operating_systems\\src\\main\\resources\\${filename}"

    fun exists() =
        Path(filePath).exists()

    open fun create() =
        Path(filePath).createFile()

    fun append(text: String) {
        val file = getFile()
        if (file.readText().isEmpty())
            file.writeText(text)
        else
            file.appendText("\n$text")
    }

    fun write(text: String) =
        getFile().writeText(text)

    open fun readAll() =
        checkFile()?.readText()

    fun readLine(numberLine: Int): String? {
        val lines = checkFile()?.readLines() ?: return null
        return if (numberLine >= lines.size || numberLine < 0) ""
        else lines[numberLine]
    }

    open fun delete() =
        Path(filePath).deleteIfExists()



    private fun getFile(): Path =
        checkFile() ?: create()

    private fun checkFile(): Path? {
        val path = Path(filePath)
        return if (path.exists())
            path
        else
            null
    }
}


class Json: File("Person.json") {

    fun write(jsonElement: JsonElement) =
        write(jsonElement.toString())

    fun readProperty(property: String): String? {
        val all = readAll() ?: return null
        val value = Person.fromJson(all)[property] ?: return "Такого свойства нет!"

        return "$property = $value"
    }
}



class Xml: File("$root.xml") {
    companion object {
        private const val root = "Persons"
    }

    private val txt = File(root + "_xml.txt")

    override fun create(): Path {
        val path = super.create()
        val xmlHeader = xml(root, encoding = "UTF-8", version = XmlVersion.V10).toString()

        if (!txt.exists())
            txt.create()
        path.writeText(xmlHeader)
        return path
    }

    fun write(person: Person) {
        txt.append(person.toString())
        updateAll()
    }

    override fun readAll(): String? {
        val persons = txt.readAll()?.split("\n") ?: return null
        val builder = StringBuilder()

        persons.forEach {
            if (it.isNotEmpty()) {
                val person = Person.fromString(it)
                builder.append("${person.toPrintString()}\n")
            }
        }

        return builder.toString()
    }

    fun readPerson(key: String): List<Person> {
        val strings = txt.readAll()?.split("\n") ?: return ArrayList()
        val persons = ArrayList<Person>()

        strings.forEach {
            if (it.contains(key))
                persons.add(Person.fromString(it))
        }
        return persons
    }

    override fun delete(): Boolean {
        txt.delete()
        return super.delete()
    }


    private fun updateAll() {
        val persons = txt.readAll()?.split("\n") ?: ArrayList()

        val text = xml(root, encoding = "UTF-8", version = XmlVersion.V10) {
            persons.forEach {
                if (it.isNotEmpty())
                    Person.fromString(it).toXml(this)
            }
        }.toString()

        write(text)
    }
}