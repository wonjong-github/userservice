package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user-service")
@RequiredArgsConstructor
public class UserController {

    private final Environment env;
    private final Greeting greeting;
    private final UserService userService;
    private final ModelMapper mapper;

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's working in User Service on Port %s",
                env.getProperty("local.server.port"));
    }

    @GetMapping("/welcome")
    public String welcome() {
//        return env.getProperty("greeting.message");
        return greeting.getMessage();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users")
    public ResponseUser createUser(@RequestBody RequestUser user) {
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserDto userDto = mapper.map(user, UserDto.class);
        UserDto createdUser = userService.createUser(userDto);
        return mapper.map(createdUser, ResponseUser.class);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users")
    public List<ResponseUser> getUsers() {
        Iterable<UserEntity> userByAll = userService.getUserByAll();

        List<ResponseUser> result = new ArrayList<>();
        userByAll.forEach(v -> {
            result.add(mapper.map(v, ResponseUser.class));
        });
        return result;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users/{userId}")
    public ResponseUser getUser(@PathVariable("userId") String userId) {
        UserDto userDto = userService.getUserByUserId(userId);
        ResponseUser result = mapper.map(userDto, ResponseUser.class);
        return result;
    }
}
