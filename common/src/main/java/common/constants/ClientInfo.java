package common.constants;

import org.springframework.beans.factory.annotation.Value;

public class ClientInfo {
    public static class UsersService {
        public static final String SERVICE_NAME= "users";;
        public static final String SERVICE_CONTEXT_ID= "users-service"; ;
    }

    public static class NotificationService {
        public static final String SERVICE_NAME = "notification";
        public static final String SERVICE_CONTEXT_ID = "notification-service";
    }
}
