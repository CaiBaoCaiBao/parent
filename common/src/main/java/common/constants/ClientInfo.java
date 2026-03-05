package common.constants;

import org.springframework.beans.factory.annotation.Value;

public class ClientInfo {
    public static class UsersService {
        @Value("${users.service.name}")
        public static final String SERVICE_NAME= "users";;
        @Value("${users.service.context-id}")
        public static final String SERVICE_CONTEXT_ID= "users-service"; ;
    }

    public static class NotificationService {
        @Value("${notification.service.name}")
        public static final String SERVICE_NAME = "notification";
        @Value("${notification.service.context-id}")
        public static final String SERVICE_CONTEXT_ID = "notification-service";
    }
}
