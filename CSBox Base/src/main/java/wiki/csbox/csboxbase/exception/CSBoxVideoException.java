package wiki.csbox.csboxbase.exception;

import lombok.Data;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 自定义异常类（全局异常类）
 * @date 2023/3/20 0020 17:45
 */
@Data
public class CSBoxVideoException extends RuntimeException{

    private String errMessage;

    public CSBoxVideoException() {
    }

    public CSBoxVideoException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public static void cast(String errMessage) {
        throw new CSBoxVideoException(errMessage);
    }
}
