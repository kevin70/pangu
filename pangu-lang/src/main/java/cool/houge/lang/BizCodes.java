package cool.houge.lang;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * 业务错误状态定义及使用.
 * <p>
 * 参考 gRPC 错误状态码实现 <a href="https://github.com/grpc/grpc/blob/master/doc/statuscodes.md">https://github.com/grpc/grpc/blob/master/doc/statuscodes.md</a>.
 * @author ZY (kzou227@qq.com)
 */
public enum BizCodes implements BizCode {

    /**
     * 未知错误。例如，当从另一个地址空间接收的状态值属于此地址空间中未知的错误空间时，可能返回此错误。此外，不返回足够错误信息的 API 引发的错误可能被转换为这个错误。
     */
    UNKNOWN(1050002, "未知错误"),
    /**
     * 客户端指定了无效的参数。
     * <p>
     * 请注意，这与 {@link #FAILED_PRECONDITION} 不同。 {@code INVALID_ARGUMENT } 表明的是，无论系统状态如何，都是有问题的参数（例如，文件名格式错误）。
     */
    INVALID_ARGUMENT(1040003, "指定了无效的参数"),
    /**
     * 操作完成之前已超过截止期限。
     * <p>
     * 对于改变系统状态的操作，即使操作已成功完成，也可能会返回此错误。例如，来自服务器的成功响应可能被延迟了很长时间。
     */
    DEADLINE_EXCEEDED(1040004, "截止时间已过期"),
    /**
     * 一些请求的实体（例如文件或目录）未找到。
     * <p>
     * 给服务器开发者的注意事项：如果针对整个用户类别（例如逐步功能推出或未记录的允许列表）拒绝了某个请求，应使用 {@code NOT_FOUND}。
     * 如果针对某个用户类别中的某些用户拒绝了请求，例如基于用户的访问控制，应使用 {@link #PERMISSION_DENIED}。
     */
    NOT_FOUND(1040405, "找不到某些请求的实体"),
    /**
     * 客户端尝试创建的实体（例如文件或目录）已经存在。
     */
    ALREADY_EXISTS(1040906, "尝试创建的实体已存在"),
    /**
     * 调用者没有执行指定操作的权限。
     * <p>
     * {@code PERMISSION_DENIED} 不得用于因资源耗尽而拒绝访问的情况（对于这些错误，应使用 {@link #RESOURCE_EXHAUSTED}）。
     * 如果无法识别调用者，则不得使用 {@code PERMISSION_DENIED} 对于这些错误，应使用 {@link #UNAUTHENTICATED}）。
     * 此错误代码并不意味着请求有效、请求实体存在或满足其他前置条件。
     */
    PERMISSION_DENIED(1040307, "调用方没有执行指定操作的权限"),
    /**
     * 某些资源已耗尽，可能是每个用户的配额，也可能是整个文件系统空间不足。
     */
    RESOURCE_EXHAUSTED(1050008, "资源已耗尽"),
    /**
     * 操作被拒绝，因为系统未处于执行操作所需的状态。
     * <p>
     * 例如，要删除的目录不为空，对非目录应用了 {@code rmdir} 操作等。
     * 服务实现者可以使用以下准则在 {@code FAILED_PRECONDITION} 、 {@link #ABORTED} 和 {@link #UNAVAILABLE} 之间进行选择：
     * <ul>
     *   <li>(a) 如果客户端只能重试失败的调用，则使用 {@link #UNAVAILABLE}；</li>
     *   <li>(b) 如果客户端应该在更高级别重试，则使用 {@link #ABORTED} 例如，当客户端指定的测试和设置失败时，表示客户端应该重新启动读取-修改-写入序列）；</li>
     *   <li>(c) 如果客户端在系统状态明确修复之前不应重试，则使用 {@code FAILED_PRECONDITION} 例如，如果由于目录不为空而导致 {@code rmdir} 失败，则应返回 {@code FAILED_PRECONDITION} 因为除非从目录中删除文件，否则客户端不应重试。</li>
     * </ul>
     */
    FAILED_PRECONDITION(1040309, "操作被拒绝"),
    /**
     * 操作被中止，通常是由于并发性问题，如顺序检查失败或事务中止。
     * <p>
     * 请按照上述准则在 {@link #FAILED_PRECONDITION} 、{@code ABORTED} 和 {@link #UNAVAILABLE} 之间进行选择。
     */
    ABORTED(1050010, "操作已中止"),
    /**
     * 尝试操作超出了有效范围。
     * <p>
     * 例如，查找或读取超出了文件结尾。与 {@link #INVALID_ARGUMENT} 不同，此错误表明如果系统状态发生变化，可以解决问题。
     * 例如，32位文件系统将在读取偏移不在[0，2^32-1]范围内的请求时生成 {@link #INVALID_ARGUMENT}，
     * 但在读取偏移超出当前文件大小的请求时会生成 {@code OUT_OF_RANGE}。{@link #FAILED_PRECONDITION} 和 {@code OUT_OF_RANGE} 之间存在一定程度的重叠。
     * 我们建议在适用的情况下使用 {@code OUT_OF_RANGE}（更具体的错误），以便在迭代空间中的调用者可以轻松地查找 {@code OUT_OF_RANGE} 错误以检测是否已经完成。
     */
    OUT_OF_RANGE(1040011, "尝试操作超出了有效范围"),
    /**
     * 该操作未被实现或在此服务中不受支持/未启用。
     */
    UNIMPLEMENTED(1040012, "操作未被实现或在此服务中不受支持/未启用"),
    /**
     * 内部错误。这意味着底层系统期望的一些不变量已被破坏。此错误代码专用于严重错误。
     */
    INTERNAL(1050013, "内部错误"),
    /**
     * 服务当前不可用。这很可能是暂时的情况，可以通过延迟重试进行纠正。请注意，对于非幂等操作来说，重试不一定总是安全的。
     */
    UNAVAILABLE(1050314, "服务当前不可用"),
    /**
     * 无法恢复的数据丢失或损坏。
     */
    DATA_LOSS(1050015, "无法恢复的数据丢失或损坏"),
    /**
     * 该请求没有有效的身份验证凭据来执行操作。
     */
    UNAUTHENTICATED(1040116, "没有有效的身份验证凭据"),
    ;

    private final int code;
    private final String message;

    BizCodes(int code, String message) {
        requireNonNull(message);
        this.code = code;
        this.message = message;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public Optional<String> subcode() {
        return Optional.empty();
    }

    /**
     * 使用子业务码.
     * @param subcode 子业务码
     * @return 业务码
     */
    public BizCode subcode(Object subcode) {
        requireNonNull(subcode);
        return new BizCode() {
            @Override
            public int code() {
                return code;
            }

            @Override
            public String message() {
                return message;
            }

            @Override
            public Optional<String> subcode() {
                return Optional.of(subcode).map(String::valueOf);
            }

            @Override
            public String toString() {
                return BizCodes.this.name();
            }
        };
    }
}
