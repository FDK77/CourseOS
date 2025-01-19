package com.example.service.impl;

import com.example.dto.Course;
import com.example.repository.CourseRepository;
import com.example.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;

    @Override
    public Course create(Course course) {
        try {
            return courseRepository.save(course);
        } catch (Exception e) {
            log.error("Failed to create course: {}", course, e);
            throw e;
        }
    }

    @Override
    public Page<Course> getAll(Pageable pageable) {
        try {
            return courseRepository.findAll(pageable);
        } catch (Exception e) {
            log.error("Failed to fetch courses: {}", pageable, e);
            throw e;
        }
    }

    @Override
    public Course get(Long id) {
        try {
            return courseRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found by id : " + id));
        } catch (Exception e) {
            log.error("Failed to fetch course with id: {}", id, e);
            throw e;
        }
    }

    @Override
    public Course update(Long id, Course updateCourse) {
        try {
            Course course = get(id);
            if (updateCourse.getTitle() != null) course.setTitle(updateCourse.getTitle());
            if (updateCourse.getHours() != null) course.setHours(updateCourse.getHours());
            if (updateCourse.getInstructor() != null) course.setInstructor(updateCourse.getInstructor());
            return courseRepository.save(course);
        } catch (Exception e) {
            log.error("Failed to update course with id: {}, data: {}", id, updateCourse, e);
            throw e;
        }
    }

    @Override
    public void delete(Long id) {
        try {
            Course course = get(id);
            courseRepository.deleteById(course.getId());
        } catch (Exception e) {
            log.error("Failed to delete course with id: {}", id, e);
            throw e;
        }
    }
}
