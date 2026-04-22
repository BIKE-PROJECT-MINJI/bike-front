package com.bikeprojectminji.bikefront.course;

public interface CourseShareGateway {

    void shareCourse(String accessToken, long courseId, Callback callback);

    interface Callback {
        void onSuccess(ShareResult result);

        void onFailure(String message);
    }

    class ShareResult {
        private final String shareType;
        private final String visibility;
        private final String shareUrl;
        private final String shareToken;

        public ShareResult(String shareType, String visibility, String shareUrl, String shareToken) {
            this.shareType = shareType;
            this.visibility = visibility;
            this.shareUrl = shareUrl;
            this.shareToken = shareToken;
        }

        public String getShareType() {
            return shareType;
        }

        public String getVisibility() {
            return visibility;
        }

        public String getShareUrl() {
            return shareUrl;
        }

        public String getShareToken() {
            return shareToken;
        }
    }
}
