package common.dict;

public class DictConstants {
    public static final class UserRole {
        public static final String ADMIN = "admin";
        public static final String USER = "user";
    }

    public static final class UserStatus {
        public static final String ACTIVE = "active";
        public static final String INACTIVE = "inactive";
    }

    public static final class TravelNoteStatus {
        public static final Integer PENDING = 0;  // 待审核
        public static final Integer PUBLISHED = 1; // 已发布
        public static final Integer REJECTED = 2;  // 已驳回
    }
}
