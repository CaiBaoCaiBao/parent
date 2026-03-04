package common.utils;

import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Feign 解码器
 */
public class FeignDecodeResult extends ResponseEntityDecoder {
    public FeignDecodeResult(Decoder decoder) {
        super(decoder);
    }

    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        // 获取原始解析结果
        Object object = super.decode(response, type);

        if (object instanceof Result) {
            Result<?> result = (Result<?>) object;
            // 直接返回 Result 对象，让调用方根据业务码判断成功或失败
            // HTTP 状态码由 SpringDecoder 处理，如果不是 2xx 会抛出 FeignException
            return result;
        }
        return object;
    }
}
