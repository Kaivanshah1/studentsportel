package com.liquifysolutions.studentportel.service

import com.liquifysolutions.studentportel.domain.Student
import com.liquifysolutions.studentportel.domain.WeatherResponse
import com.liquifysolutions.studentportel.repository.CustomStudentRepository
import com.liquifysolutions.studentportel.repository.StudentRepository
import com.opencsv.CSVReader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*


@Service
class StudentService(private val studentRepository: StudentRepository,
                     private val customStudentRepository: CustomStudentRepository,
                     private val javaMailSender: JavaMailSender
) {
    fun createStudent(student: Student): Student{
        val studentToCreate = student.copy(id=UUID.randomUUID().toString())
        return studentRepository.save(studentToCreate)
    }

    fun sendConfirmationEmail(toEmail: String) {
        val message = SimpleMailMessage()
        message.setFrom("shahkai999@gmail.com")
        message.setTo(toEmail)
        message.setSubject("Welcome to Student Portal")
        message.setText("Hello,\n\nThank you for registering with the Student Portal. We are excited to have you onboard!")

        javaMailSender.send(message)
    }

    fun uploadAndSaveCSV(file: InputStream): String {
        val requiredColumns = listOf("name", "age", "gender", "email", "assignedClass")
        val reader = CSVReader(InputStreamReader(file))
        val header = reader.readNext()

        if (header == null) {
            throw IllegalArgumentException("header not present")
        }

        if (header.size != requiredColumns.size) {
            throw IllegalArgumentException("size mismatch between headers")
        }

        header.forEachIndexed { index, headerItem ->
            if (headerItem.trim() != requiredColumns[index]) {
                throw IllegalArgumentException("mismatch for $headerItem not as per requirement")
            }
        }

        val existingEmails = studentRepository.findByEmailIn(header.drop(3)).map { it.email }.toSet()

        val students = reader.readAll().mapNotNull { record ->
                val age = record[1].toInt()
                val email = record[3]

                if (email !in existingEmails) {
                    Student(
                        name = record[0],
                        email = email,
                        gender = record[2],
                        assignedClass = record[4],
                        age = age
                    )
                } else {
                    null // Skip if email exists
                }
            }

        if (students.isNotEmpty()) {
            studentRepository.saveAll(students)
        }

        return "${students.size} new records uploaded"
    }

    fun generateStudentPdf(id: String): ByteArray {
        val student: Student = studentRepository.findById(id).orElseThrow { IllegalArgumentException("not found") }

        PDDocument().use { document ->
            val page = PDPage()
            document.addPage(page)

            PDPageContentStream(document, page).use { contentStream ->
                contentStream.beginText()
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14f)
                contentStream.newLineAtOffset(50f, 750f)

                // Centered Title
                contentStream.showText("Student Details")
                contentStream.newLineAtOffset(0f, -30f)

                // Vertical Layout for Student Data
                contentStream.setFont(PDType1Font.HELVETICA, 12f)

                contentStream.showText("Name: ${student.name}")
                contentStream.newLineAtOffset(0f, -20f)
                contentStream.showText("Age: ${student.age}")
                contentStream.newLineAtOffset(0f, -20f)
                contentStream.showText("Email: ${student.email}")
                contentStream.newLineAtOffset(0f, -20f)
                contentStream.showText("Gender: ${student.gender}")
                contentStream.newLineAtOffset(0f, -20f)
                contentStream.showText("Assigned Class: ${student.assignedClass}")
                contentStream.newLineAtOffset(0f, -20f)

                contentStream.endText()
            }

            val outputStream = ByteArrayOutputStream()
            document.save(outputStream)

            return outputStream.toByteArray()
        }
    }

    fun getAllStudents(page: Int, size: Int): Page<Student>{
       return studentRepository.findAll(PageRequest.of(page, size))
    }

    fun getStudentById(id: String): Student{
        return studentRepository.findById(id).orElseThrow{IllegalArgumentException("Not a valid id")}
    }

    fun listStudents(
        nameSearch: String? = null,
        age: Int? = null,
        assignedClass: List<String>? = null,
        email: String? = null,
        gender: String?= null,
        page: Int = 0,
        size: Int = 10
    ): Page<Student> {
        val pageable = PageRequest.of(page, size)
        return customStudentRepository.searchAndFilter(
            name = nameSearch,
            age = age,
            assignedClass = assignedClass,
            email = email,
            gender=gender,
            pageable = pageable
        )
    }

    fun updateStudent(id: String, student: Student): Student{
        val studentInDB = studentRepository.findById(id)
        if (studentInDB.isPresent) {
            return studentRepository.save(student)
        } else {
            throw IllegalArgumentException("Student with ID $id does not exist")
        }
    }

    fun deleteStudent(id: String) {
        if(studentRepository.existsById(id)){
            studentRepository.deleteById(id)
        }else{
            throw IllegalArgumentException("No student found with id: $id")
        }
    }
}

@Service
class WeatherService(
    private val restTemplate: RestTemplate
) {

    @Value("\${weatherstack.access_key}")
    lateinit var accessKey: String

    fun getWeather(query: String): WeatherResponse? {
        val url = "https://api.weatherstack.com/current?access_key=$accessKey&query=$query"
        return restTemplate.getForObject(url, WeatherResponse::class.java)
    }
}