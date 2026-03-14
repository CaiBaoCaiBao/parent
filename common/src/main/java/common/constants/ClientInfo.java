package common.constants;

import org.springframework.beans.factory.annotation.Value;

public class ClientInfo {
    public static class UsersService {
        public static final String SERVICE_NAME= "users";;
        public static final String SERVICE_CONTEXT_ID= "users-service"; ;
    }

    public static class ContentService {
        public static final String SERVICE_NAME= "content";;
        public static final String SERVICE_CONTEXT_ID= "content-service"; ;
    }

    public static class NotificationService {
        public static final String SERVICE_NAME = "notification";
        public static final String SERVICE_CONTEXT_ID = "notification-service";
    }

    public static class FileService {
        public static final String SERVICE_NAME = "file";
        public static final String SERVICE_CONTEXT_ID = "file-service";
    }

    public static class SocialService {
        public static final String SERVICE_NAME= "social";;
        public static final String SERVICE_CONTEXT_ID= "social-service"; ;
    }
}
