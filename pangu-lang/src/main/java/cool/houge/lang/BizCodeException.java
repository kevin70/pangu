package cool.houge.lang;

import lombok.Getter;

/**
 * 带业务码的异常.
 *
 * @author ZY (kzou227@qq.com)
 */
@Getter
public final class BizCodeException extends HougeException {

    /**
     * 业务错误码.
     */
    private final BizCode code;

    /**
     * 使用业务码构建异常.
     *
     * @param code 业务码
     */
    public BizCodeException(BizCode code) {
        super(code.toString());
        this.code = code;
    }

    /**
     * 使用业务码、描述构建异常.
     *
     * @param code    业务码
     * @param message 描述
     */
    public BizCodeException(BizCode code, String message) {
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
    public BizCodeException(BizCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 返回原始的错误描述.
     *
     * @return 错误描述
     */
    public String getRawMessage() {
        return super.getMessage();
    }

    /**
     * 返回格式化的错误描述.
     *
     * @return 错误描述
     */
    @Override
    public String getMessage() {
        return getFormattedMessage(this.getRawMessage());
    }

    /**
     * 返回格式化的错误描述.
     *
     * @return 错误描述
     */
    @Override
    public String toString() {
        return this.getMessage();
    }

    private String getFormattedMessage(final String rawMessage) {
        final StringBuilder builder = new StringBuilder(64);
        if (rawMessage != null) {
            builder.append(rawMessage).append(" | ");
        }
        builder.append(this.code.code()).append(":").append(this.code.message());

        this.code.subcode().ifPresent(subcode -> builder.append(" | subcode:").append(subcode));
        return builder.toString();
    }
}
