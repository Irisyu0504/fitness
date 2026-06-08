package org.example.fitness_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.fitness_backend.entity.User;

/**
 * 用户服务接口。
 * <p>提供用户的增删改查及认证相关的业务能力，
 * 继承 MyBatis-Plus 的 IService 以获得通用 CRUD 方法。</p>
 */
public interface UserService extends IService<User> {

}