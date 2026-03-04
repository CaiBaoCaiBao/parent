package users.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import users.mapper.AuthMapper;
import users.pojo.entity.Users;
import users.service.AuthService;

@Slf4j
@Service
public class AuthServiceImpl extends ServiceImpl<AuthMapper, Users> implements AuthService {
    @Autowired
    AuthMapper authMapper;
}
