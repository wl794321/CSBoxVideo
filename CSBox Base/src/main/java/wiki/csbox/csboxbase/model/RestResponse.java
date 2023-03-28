package wiki.csbox.csboxbase.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO
 * @date 2023/3/28 0028 13:10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RestResponse<T> {

    /**
     * 响应编码,0为正常,-1错误
     */
    private int code;

    /**
     * 响应提示信息
     */
    private String msg;

    /**
     * 响应内容
     */
    private T result;

    /**
     * 错误信息的封装
     *
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> RestResponse<T> validfail(String msg) {
        RestResponse<T> response = new RestResponse<T>();
        response.setCode(-1);
        response.setMsg(msg);
        return response;
    }

    public static <T> RestResponse<T> validfail(T result, String msg) {
        RestResponse<T> response = new RestResponse<T>();
        response.setCode(-1);
        response.setResult(result);
        response.setMsg(msg);
        return response;
    }

    /**
     * 添加正常响应数据（包含响应内容）
     *
     * @return RestResponse Rest服务封装相应数据
     */
    public static <T> RestResponse<T> success(T result) {
        RestResponse<T> response = new RestResponse<T>();
        response.setResult(result);
        return response;
    }

    public static <T> RestResponse<T> success(T result, String msg) {
        RestResponse<T> response = new RestResponse<T>();
        response.setResult(result);
        response.setMsg(msg);
        return response;
    }

    /**
     * 添加正常响应数据（不包含响应内容）
     *
     * @return RestResponse Rest服务封装相应数据
     */
    public static <T> RestResponse<T> success() {
        return new RestResponse<T>();
    }

    public Boolean isSuccessful() {
        return this.code == 0;
    }
}
