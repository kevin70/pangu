package cool.houge.lang;

/**
 * Houge 异常.
 *
 * @author ZY (kzou227@qq.com)
 */
public class HougeException extends RuntimeException {

    /**
     * 使用错误描述创建异常.
     *
     * @param message 错误描述
     */
    public HougeException(String message) {
        super(message);
    }

    /**
     * 使用错误描述与目标异常创建异常.
     * <p>
     * 带目标异常的不会使用{@link #fillInStackTrace()}填充堆栈.
     *
     * @param message 错误描述
     * @param cause   目标异常
     */
    public HougeException(String message, Throwable cause) {
        super(message, cause, false, false);
    }
}
