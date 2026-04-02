package com.bikeprojectminji.bikefront.course;

public interface CourseWriteGateway {

    void createCourse(String accessToken, CreateCourseDraft draft, Callback callback);

    interface Callback {
        void onSuccess(CourseCreateResult result);

        void onFailure(String message);
    }

    class CreateCourseDraft {
        private final long sourceRideRecordId;
        private final String name;
        private final String description;
        private final String visibility;

        public CreateCourseDraft(long sourceRideRecordId, String name, String description, String visibility) {
            this.sourceRideRecordId = sourceRideRecordId;
            this.name = name;
            this.description = description;
            this.visibility = visibility;
        }

        public long getSourceRideRecordId() { return sourceRideRecordId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getVisibility() { return visibility; }
    }

    class CourseCreateResult {
        private final long courseId;
        private final String visibility;

        public CourseCreateResult(long courseId, String visibility) {
            this.courseId = courseId;
            this.visibility = visibility;
        }

        public long getCourseId() { return courseId; }
        public String getVisibility() { return visibility; }
    }
}
