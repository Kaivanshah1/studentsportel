package com.liquifysolutions.studentportel.controller

import com.liquifysolutions.studentportel.domain.ListStudentsInput
import com.liquifysolutions.studentportel.domain.Student
import com.liquifysolutions.studentportel.domain.WeatherResponse
import com.liquifysolutions.studentportel.repository.StudentRepository
import com.liquifysolutions.studentportel.service.StudentService
import com.liquifysolutions.studentportel.service.WeatherService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@CrossOrigin
@RestController
@RequestMapping("/api/v1/student")
class StudentController(private val studentService: StudentService, private val studentRepository: StudentRepository){

    @GetMapping
    fun getAllStudents(@RequestParam(defaultValue = "0") page: Int, @RequestParam(defaultValue = "1") size: Int): Page<Student>{
        return studentService.getAllStudents(page, size)
    }

    @GetMapping("/get/{id}")
    fun getStudent(@PathVariable id: String): ResponseEntity<Student> {
        val student = studentService.getStudentById(id)
        return ResponseEntity.ok(student)
    }

    @PostMapping("/list")
    fun listStudents(
        @RequestBody listStudentsInput: ListStudentsInput,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<Student> {
        val students = studentService.listStudents(
            nameSearch = listStudentsInput.nameSearch,
            age = listStudentsInput.age,
            assignedClass = listStudentsInput.assignedClass,
            email=listStudentsInput.email,
            gender=listStudentsInput.gender,
            page=page,
            size=size
        )
        return students
    }

    @PostMapping("/upload")
    fun uploadCSVFile(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        try {
            val result = studentService.uploadAndSaveCSV(file.inputStream)
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping
    fun createStudent(@RequestBody student: Student){
        studentService.sendConfirmationEmail(student.email)
        ResponseEntity.ok(studentService.createStudent(student))
    }

    //---------------------------------------------------------//

    @GetMapping("/{id}/download-pdf")
    fun downloadStudentPdf(@PathVariable id: String): ResponseEntity<ByteArray> {
        try {
            val pdfByteArray = studentService.generateStudentPdf(id)
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_PDF
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=student_$id.pdf")

            return ResponseEntity.ok().headers(headers).body(pdfByteArray)
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().build()
        }
    }

    @PutMapping("/{id}")
    fun updateStudent(@RequestBody student: Student, @PathVariable id: String) {
        studentService.updateStudent(id = id, student = student)
    }

    @DeleteMapping("/{id}")
    fun deleteStudent(@PathVariable id: String){
        studentService.deleteStudent(id=id)
    }
}

@RestController
class WeatherController(
    private val weatherService: WeatherService
) {

    @GetMapping("/weather")
    fun getWeather(@RequestParam query: String): WeatherResponse? {
        return weatherService.getWeather(query)
    }
}