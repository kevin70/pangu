package person;

import jakarta.validation.constraints.NotNull;

/**
 * 更新用户
 * @param id 用户ID
 * @param firstname 首名字
 * @param lastname 尾名字
 */
public record PersonUForm(int id, @NotNull String firstname, String lastname) {}
