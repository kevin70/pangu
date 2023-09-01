package cool.houge.lang;

/**
 * 带业务码的异常.
 *
 * @author ZY (kzou227@qq.com)
 */
public class BizCodeException extends HougeException {

    private final int code;

    /**
     * 使用业务码构建异常.
     *
     * @param code 业务码
     */
    public BizCodeException(int code) {
        super("biz code: " + code);
        this.code = code;
    }

    /**
     * 使用业务码、描述构建异常.
     *
     * @param code    业务码
     * @param message 描述
     */
    public BizCodeException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用业务码、描述、原因信息构建异常.
     *
     * @param code    业务码
     * @param message 描述
     * @param cause   原因
     */
    public BizCodeException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 返回业务码.
     *
     * @return 业务码
     */
    public int getCode() {
        return code;
    }
}
