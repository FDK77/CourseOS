package com.example.controller;

import com.example.dto.CourseCreateRequest;
import com.example.dto.CourseDto;
import com.example.mapper.CourseMapper;
import com.example.rabbit.MessageProducer;
import com.example.rabbit.model.Message;
import com.example.rabbit.model.Operation;
import com.example.service.grpc.CourseOuterClass;
import com.example.service.grpc.CourseServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.configuration.RedisConfig.REDIS_KEY;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course")
@Slf4j
public class GatewayController {

    private final CourseServiceGrpc.CourseServiceBlockingStub courseServiceStub;
    private final CourseMapper courseMapper;
    private final MessageProducer messageProducer;

    private static final int GRPC_TIMEOUT_SECONDS = 3;

    @GetMapping("/{id}")
    @Cacheable(value = REDIS_KEY, key = "#id")
    public CourseDto getById(@PathVariable Long id) {
        log.info("GET request for getting course with (id: {})", id);
        try {

            CourseServiceGrpc.CourseServiceBlockingStub stubWithTimeout =
                    courseServiceStub.withDeadlineAfter(GRPC_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);

            CourseOuterClass.GetCourseRequest request = CourseOuterClass.GetCourseRequest.newBuilder().setId(id).build();
            CourseOuterClass.CourseResponse response = stubWithTimeout.getCourse(request);
            return courseMapper.toDto(response.getCourse());
        } catch (Exception e) {
            log.error("Error retrieving course with id: {}", id, e);
            throw new RuntimeException("Failed to retrieve course");
        }
    }

    @GetMapping
    @Cacheable(value = REDIS_KEY, key = "#page + '-' + #size")
    public List<CourseDto> getAll(@RequestParam int page, @RequestParam int size) {
        log.info("GET request for getting courses with (page: {}, size: {})", page, size);
        try {
            CourseServiceGrpc.CourseServiceBlockingStub stubWithTimeout =
                    courseServiceStub.withDeadlineAfter(GRPC_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);

            CourseOuterClass.GetAllCoursesRequest request = CourseOuterClass.GetAllCoursesRequest.newBuilder().setPage(page).setSize(size).build();
            CourseOuterClass.CourseListResponse courseListResponse = stubWithTimeout.getAllCourses(request);
            return courseListResponse.getCoursesList().stream().map(courseMapper::toDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving courses", e);
            throw new RuntimeException("Failed to retrieve courses");
        }
    }

    @PostMapping
    @CacheEvict(value = REDIS_KEY, allEntries = true)
    public ResponseEntity<?> create(@RequestBody CourseCreateRequest data) {
        log.info("POST request for creating (course: {})", data);
        try {
            messageProducer.sendMessage(Message.builder().operation(Operation.CREATE).data(data).build());
            return ResponseEntity.accepted().body("Create request sent.");
        } catch (Exception e) {
            log.error("Error creating course", e);
            throw new RuntimeException("Failed to create course");
        }
    }

    @PatchMapping("/{id}")
    @CacheEvict(value = REDIS_KEY, allEntries = true)
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CourseDto data) {
        log.info("PATCH request for updating (id: {}, course: {})", id, data);
        try {
            data.setId(id);
            messageProducer.sendMessage(Message.builder().operation(Operation.UPDATE).data(data).build());
            return ResponseEntity.accepted().body("Update request sent.");
        } catch (Exception e) {
            log.error("Error updating course with id: {}", id, e);
            throw new RuntimeException("Failed to update course");
        }
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = REDIS_KEY, allEntries = true)
    public ResponseEntity<?> delete(@PathVariable Long id) {
        log.info("DELETE request for deleting course with (id: {})", id);
        try {
            messageProducer.sendMessage(Message.builder().operation(Operation.DELETE).data(id).build());
            return ResponseEntity.accepted().body("Delete request sent.");
        } catch (Exception e) {
            log.error("Error deleting course with id: {}", id, e);
            throw new RuntimeException("Failed to delete course");
        }
    }
}
