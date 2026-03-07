package common.utils;

import com.github.f4b6a3.ulid.UlidCreator;

public class ULIDUtils {
    
    private ULIDUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
    
    public static String generateULID(){
        return UlidCreator.getUlid().toString();
    }
}
