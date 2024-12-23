package com.liquifysolutions.studentportel.repository

import com.liquifysolutions.studentportel.domain.Student
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate

@Repository
interface StudentRepository : MongoRepository<Student, String> {
    fun findByEmailIn(emails: List<String>): List<Student>
}

@Component
class CustomStudentRepository(private val mongoTemplate: MongoTemplate) {
    fun searchAndFilter(
        name: String?,
        age: Int?,
        assignedClass: List<String>?,
        email: String?,
        gender: String?,
        pageable: Pageable
    ): Page<Student> {
        val query = Query()
        if (!name.isNullOrEmpty()) {
            query.addCriteria(Criteria.where("name").regex(name, "i"))
        }
        if (age != null) {
            query.addCriteria(Criteria.where("age").`is`(age))
        }
        if (!assignedClass.isNullOrEmpty()) {
            query.addCriteria(Criteria.where("assignedClass").`in`(assignedClass))
        }
        if(!email.isNullOrEmpty()){
            query.addCriteria(Criteria.where("email").regex(email, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$"))
        }

        query.with(pageable)

        val students = mongoTemplate.find(query, Student::class.java)
        val total = mongoTemplate.count(query.skip(0).limit(0), Student::class.java)

        return PageImpl(students, pageable, total)
    }
}


