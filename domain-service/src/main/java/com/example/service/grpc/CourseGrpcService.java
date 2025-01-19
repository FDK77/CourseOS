package com.example.service.grpc;

import com.example.dto.Course;
import com.example.service.CourseService;
import com.example.service.grpc.CourseOuterClass.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourseGrpcService extends CourseServiceGrpc.CourseServiceImplBase {
    private final CourseService courseService;

    @Override
    public void createCourse(CreateCourseRequest request,
                             StreamObserver<CourseResponse> responseObserver) {
        Course course = courseService.create(new Course(null, request.getTitle(), request.getInstructor(), request.getHours()));

        responseObserver.onNext(CourseResponse.newBuilder().setCourse(map(course)).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllCourses(GetAllCoursesRequest request, StreamObserver<CourseListResponse> responseObserver) {
        try {
            Page<Course> page = courseService.getAll(PageRequest.of(request.getPage(), request.getSize() == 0 ? 10 : request.getSize()));
            responseObserver.onNext(CourseListResponse.newBuilder().addAllCourses(page.getContent()
                    .stream()
                    .map(this::map)
                    .collect(Collectors.toList())).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error retrieving the courses", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Database is unavailable")
                    .augmentDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getCourse(GetCourseRequest request, StreamObserver<CourseResponse> responseObserver) {
        try {
            Course course = courseService.get(request.getId());
            responseObserver.onNext(CourseResponse.newBuilder().setCourse(map(course)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error retrieving course with id: {}", request.getId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Database is unavailable")
                    .augmentDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void updateCourse(CourseOuterClass.UpdateCourseRequest request, StreamObserver<CourseResponse> responseObserver) {
        try {
            Course course = courseService.update(request.getId(), new Course(request.getId(), request.getTitle(), request.getInstructor(), request.getHours()));
            responseObserver.onNext(CourseResponse.newBuilder().setCourse(map(course)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error update course with id: {}", request.getId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Database is unavailable")
                    .augmentDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteCourse(GetCourseRequest request, StreamObserver<Empty> responseObserver) {
        try {
            courseService.delete(request.getId());
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        }catch (Exception e) {
            log.error("Error delete course with id: {}", request.getId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Database is unavailable")
                    .augmentDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    private CourseOuterClass.Course map(Course course) {
        return CourseOuterClass.Course.newBuilder().setId(course.getId()).setTitle(course.getTitle()).setHours(course.getHours()).setInstructor(course.getInstructor()).build();
    }
}
